#!/usr/bin/perl

# DARPA XDATA licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with 
# the License.  You may obtain a copy of the License at 
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and  
# limitations under the License.
#
# Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
#

use POSIX;
use Getopt::Long;

$aInput = shift;
$bInput = shift;
$output = shift;

#command flags
my $giraphJar;
my $jblasJar;
my $zookeeper;
my $tempDir;
my $totalIter = 20;
my $delim = ",";
my $N;
my $nR = 1;
my $nC = 1;
my $epsilon = 0.0;
my $auctionIter = 100000;
my $numReducers;
my $libgfortran;

GetOptions(
	"giraph=s" => \$giraphJar,
	"jblas=s" => \ $jblasJar,
	"zoo=s" => \$zookeeper,
	"temp=s" => \$tempDir,
	"iter=i" => \$totalIter,
	"delim=s" => \$delim,
	"n=i" => \$N,
	"nr=i" => \$nR,
	"nc=i" => \$nC,
	"e=f" => \$epsilon,
	"auction_iter=i" => \$auctionIter,
	"reducers=i" => \$numReducers,
	"libgfortran=s" => \$libgfortran
);

my $giraphMappers = $numReducers - 1;
	
#arithmetic functions
sub multiply {
	my ($left, $right, $out, $scalar, $lTrans, $rTrans) = @_;
	#create a temporary directory
	$tempPath = $out."temp";
		
	#mult-step
	#params left right output scalar ltrans rtrans nRL nCL nRR nCR numReducers
	`hadoop jar graph-matching.jar jobs.MatrixBlockMult -libjars $jblasJar -files $libgfortran $left $right $tempPath $scalar $lTrans $rTrans $nR $nC $nR $nC $numReducers`;
		
		
	#add-step
	#params
	`hadoop jar graph-matching.jar jobs.MatrixBlockSum $tempPath $out $numReducers`;
		
	#delete temp
	`hadoop fs -rm -r $tempPath`;
}
	
sub traceMult {
	my ($left, $right, $out, $scalar, $lTrans, $rTrans) = @_;
	`hadoop jar graph-matching.jar jobs.MatrixBlockTraceMult $left $right $out $scalar $lTrans $rTrans $numReducers`;
}
	
sub add {
	my ($left, $right, $out, $alpha, $beta) = @_;
	`hadoop jar graph-matching.jar jobs.MatrixBlockAdd $left $right $out $alpha $beta $numReducers`;
}
	
sub sum {
	my $text = shift;
	my $out = 0;
	my @vals = split "\n", $text;
		
	foreach (@vals) {
		$out += $_;
	}
	return $out;
}

#check that all necessary input flags exist - return an error statment or use a data
if (! ($aInput && $bInput && $output)) {
	print "usage: ./unseeded.pl inputA inputB output [flags]\n";
	exit(1);
}

if (! $giraphJar) {
	print "-giraph <giraph jar> is a required parameter\n";
	exit(1);
}

if (! $jblasJar) {
	print "-jblas <jblas jar> is a required parameter\n";
	exit(1);
}

if (! $zookeeper) {
	print "-zoo <zookeeper> required parameter\n";
	exit(1);
}

if (! $tempDir) {
	print "-temp <temporary hdfs directory> is a required parameter\n";
	exit(1);
}

if (! $N) {
	print "-n <dimension of square matrix edge> is a required parameter\n";
	exit(1);
}

if (! $libgfortran) {
	if (-e 'libgfortran.so.3') {
		$libgfortran = 'libgfortran.so.3';
	}
	else {
		print "-libgfortran <libgfortran.so.3> is a required parameter\n";
		exit(1);
	}
}
		
#get the size of the blocks - possibly change the number of blocks in a given direction
if ($N % $nR) {
	$sR = ceil($N / $nR);
		
	while ($sR * ($nR - 1) > $N) {
		$nR--;
	}
}
else {
	$sR = $N / $nR;
}
if ($N % $nC) {
	$sC = ceil($N / $nC);
	while ($sC * ($nC - 1) >= $N) {
		$nC--;
	}
}
else {
	$sC = $N / $nC;
}

#create P Matrix - N X N, each value is 1/N
$pPath = "$tempDir/P";
		
#params are N, SR, SC, delimeter, numreducers
`hadoop jar graph-matching.jar jobs.CreateUniformDoublyStochastic $tempDir/pTemp $pPath $N $sR $sC $delim $numReducers`;
`hadoop fs -rm -r $tempDir/pTemp`;
	
#load A Matrix to Blocks
$aPath = "$tempDir/A";
$bPath = "$tempDir/B";
	
#params are input output sR sC delim one numreducers
`hadoop jar graph-matching.jar jobs.EdgeListToMatrixBlock $aInput $aPath $sR $sC $delim 0 $numReducers`;
`hadoop jar graph-matching.jar jobs.EdgeListToMatrixBlock $bInput $bPath $sR $sC $delim 0 $numReducers`;
	
#start the loop
$tolerance = .99;
$continue = 1;

while ($i < $totalIter && $continue) {
		
	#grad = A22 * P * B22' + A22' * P * B22
		#a * P
		$apPath = "$tempDir/ap";
		multiply($aPath,$pPath,$apPath,1,"false","false");
		
		#^ * b'
		$apbtPath = "$tempDir/apbt";
		multiply($apPath,$bPath,$apbtPath,1,"false","true");
		`hadoop fs -rm -r $apPath`;
			
		#a' * P
		$atpPath = "$tempDir/atp";
		multiply($aPath,$pPath,$atpPath,1,"true","false");
			
		#^ * b
		$atpbPath = "$tempDir/atpb";
		multiply($atpPath,$bPath,$atpbPath,1,"false","false");
		`hadoop fs -rm -r $atpPath`;
			
		#add
		$apbtatpbPath = "$tempDir/apbtatpb";
		add ($atpbPath,$apbtPath,$apbtatpbPath, 1, 1);
		`hadoop fs -rm -r $apbtPath`;
			
		#transfer to auction text format
		$auctionIn = "$tempDir/auctionIn";
		$auctionTempIn = "$tempDir/auctionTempIn";
		`hadoop jar graph-matching.jar jobs.MatrixBlockToTextRows $apbtatpbPath $auctionTempIn $N $nC $sC $numReducers`; 
		`hadoop fs -rm -r $apbtatpbPath`;
		`hadoop fs -mkdir $auctionIn`;
		`hadoop fs -cat $auctionTempIn/* | hadoop fs -put - $auctionIn`;
		`hadoop fs -rm -r $auctionTempIn`;
			
		#call auction
		$auctionOut = "$tempDir/auctionOut";
		`hadoop jar graph-matching.jar auction.Auction -libjars $giraphJar,graph-matching.jar "-Dgiraph.zkList=$zookeeper" $auctionIn $auctionOut $epsilon $auctionIter $giraphMappers`;
		`hadoop fs -rm -r $auctionIn`;
		
		#load auction result
		$tPath = "$tempDir/T";
		`hadoop jar graph-matching.jar jobs.EdgeListToMatrixBlock $auctionOut $tPath $sR $sC $delim 0 $numReducers`;
		`hadoop fs -rm -r $auctionOut`;
		
		#trace section
		$atTPath = "$tempDir/atT";
		multiply($aPath,$tPath,$atTPath, 1,"true","false");
			
		$atTbPath = "$tempDir/atTb";
		multiply($atTPath,$bPath,$atTbPath,1,"false","false");
		`hadoop fs -rm -r $atTPath`;
		
		#c=trace(A22'*P*B22%*P');
		$traceCPath = "$tempDir/traceC";
		traceMult($atpbPath, $pPath, $traceCPath, 1, "false", "true");
		$c = `hadoop fs -cat $traceCPath/*`;
		$c = sum($c);
			
		#d=trace(A22'*T*B22*P')+trace(A22'*P*B22*T');
		$traceD1Path = "$tempDir/traceD1";
		traceMult($atTbPath, $pPath, $traceD1Path, 1, "false", "true");
		$traceD2Path = "$tempDir/traceD2";
		traceMult($atpbPath, $tPath, $traceD2Path, 1, "false", "true");
		$d1 = `hadoop fs -cat $traceD1Path/*`;
		$d2 = `hadoop fs -cat $traceD2Path/*`;
		$d = sum($d1) + sum($d2);
			
		#e=trace(A22'*T*B22*T');		
		$traceEPath = "$tempDir/traceE";
		traceMult($atTbPath, $tPath, $traceEPath, 1, "false", "true");
		$e = `hadoop fs -cat $traceEPath/*`;
		$e = sum($e);
			
		#terminate or descend
		$alpha =-($d-2*$e)/(2*($c-$d+$e));
		$f0=0;
		$f1=$c-$e;
		$falpha=($c-$d+$e)*$alpha*$alpha+($d-2*$e)*$alpha;
			
		if ($alpha<$tolerance && $alpha>0 && $falpha>$f0 && $falpha>$f1) {
		   #P=alpha*P+(1-alpha)*T;
		   $newPPath = "$tempDir/newP";
		   add($pPath, $tPath, $newPPath, $alpha, 1 - $alpha);
		   `hadoop fs -rm -r $pPath`;
		   `hadoop jar graph-matching.jar jobs.Rename $newPPath $pPath`;   
		}
		elsif ($f0 > $f1) {
			#P = T
			`hadoop fs -rm -r $pPath`;
			`hadoop jar graph-matching.jar jobs.Rename $tPath $pPath`;
		}
		else {
			#break
			$continue = 0;
		}
			
		#delete everything specific to an iteration
		`hadoop fs -rm -r $tPath`;
		`hadoop fs -rm -r $traceCPath`;
		`hadoop fs -rm -r $traceD1Path`;
		`hadoop fs -rm -r $traceD2Path`;
		`hadoop fs -rm -r $traceEPath`;
		`hadoop fs -rm -r $atTbPath`;
		`hadoop fs -rm -r $atpbPath`;
	
	$i++;
	}
		
	#transfer to auction text format
	$auctionIn = "$tempDir/auctionIn";
	$auctionTempIn = "$tempDir/auctionTempIn";
	`hadoop jar graph-matching.jar jobs.MatrixBlockToTextRows $pPath $auctionTempIn $N $nC $sC $numReducers`; 
	`hadoop fs -mkdir $auctionIn`;
	`hadoop fs -cat $auctionTempIn/* | hadoop fs -put - $auctionIn`;
	`hadoop fs -rm -r $auctionTempIn`;
			
	#call auction
	print "hadoop jar graph-matching.jar auction.Auction -libjars $giraphJar,graph-matching.jar \"-Dgiraph.zkList=$zookeeper\" $auctionIn $output .001 10 1\n";
	`hadoop jar graph-matching.jar auction.Auction -libjars $giraphJar,graph-matching.jar "-Dgiraph.zkList=$zookeeper" $auctionIn $output $epsilon $auctionIter $giraphMappers`;
	`hadoop fs -rm -r $auctionIn`;
	














