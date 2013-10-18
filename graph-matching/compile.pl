#!/usr/bin/env perl

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

$hadoopJar = shift;
$giraphJar = shift;
$jblasJar = shift;

if (! ($hadoopJar && $giraphJar && $jblasJar)) {
	print "usage: ./compile.pl <hadoop jar> <giraph jar> <jblas jar>\n";
	exit(1);
}

$base = "src/";
@dirs = ("fileformats", "mappers", "reducers", "jobs");

mkdir "target";
mkdir "target/classes";

foreach $dir (@dirs) {
	@files = <$base$dir/*>;
	foreach $file (@files) {
		`javac -cp $hadoopJar:$giraphJar:$jblasJar:target/classes -d target/classes $file`;
	}
}

$auction = $base."auction";

foreach $file(reverse sort <$auction/*>) {
	`javac -cp $hadoopJar:$giraphJar:$jblasJar:target/classes -d target/classes $file`;
}
	
`jar -cvf graph-matching.jar -C target/classes .`;
