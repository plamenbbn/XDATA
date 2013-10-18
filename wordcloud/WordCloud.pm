package WordCloud;

use strict;

sub new {
    my $class = shift;
    my $self = {
        _terms => {},
        _default_color => "black",
        _font_size_min => 12,
        _font_size_max => 52,
    };
    bless $self, $class;

    return $self;
}

sub font_size_min {
    my $self = shift;
    $self->{_font_size_min} = shift if (@_);
    return $self->{_font_size_min};
}

sub font_size_max {
    my $self = shift;
    $self->{_font_size_max} = shift if (@_);
    return $self->{_font_size_max};
}

sub default_color {
    my $self = shift;
    $self->{_default_color} = shift if (@_);
    return $self->{_default_color};
}

# return a new wordcloud object that only contains (at most) top N terms
sub get_subset {
    my $self = shift;
    my ($numTerms) = @_;
    my $scloud = WordCloud->new();
    $scloud->default_color($self->default_color());
    $scloud->font_size_min($self->font_size_min());
    $scloud->font_size_max($self->font_size_max());

    my $all_size = 1;
    foreach my $k (keys %{$self->{_terms}}) {
        $all_size = 0 unless (exists $self->{_terms}{$k}{SIZE});
    }
    my $sfield = 'TFIDF';
    $sfield = 'SIZE' if ($all_size);

    my @order = sort {$self->{_terms}{$b}{$sfield} <=> $self->{_terms}{$a}{$sfield}} keys %{$self->{_terms}};

    if (scalar(@order) > $numTerms) {
        @order = @order[0..($numTerms-1)];
    }
    foreach my $k (@order) {
        my $e = $self->{_terms}{$k};
        $scloud->add_term($k, $e->{TF}, $e->{DF}, %{$e});
    }
    return $scloud;
}

sub _get_term_hash {
    my $self = shift;
    return $self->{_terms};
}

sub merge_clouds {
    my $class = shift;
    my (@clouds) = @_;
    my $mcloud = WordCloud->new();
    foreach my $cl (@clouds) {
        foreach my $k (keys %{$cl->_get_term_hash()}) {
            my %e = %{$cl->{_terms}{$k}};
            if ((! exists($e{COLOR})) &&
                ($cl->default_color() ne "black")) {
                $e{COLOR} = $cl->default_color();
            }
            $mcloud->add_term($k, $e{TF}, $e{DF}, %e);
        }
    }
    return $mcloud;
}

sub add_term {
    my $self = shift;
    my ($term, $tf, $df, %params) = @_;
    my $e = { TERM => $term, TF => $tf, DF => $df };

    if (exists $params{TFIDF}) {
        $e->{TFIDF} = $params{TFIDF};
    } else {
        $e->{TFIDF} = _tfidf($tf, $df);
    }
    if (exists $params{COLOR}) {
        $e->{COLOR} = $params{COLOR};
    }
    if (exists $params{SIZE}) {
        $e->{SIZE} = $params{SIZE};
    }
    if (exists $params{SUB}) {
        $e->{SUB} = $params{SUB};
    }
    $self->{_terms}{$term} = $e;
}

sub _tfidf {
    my ($tf, $df) = @_;
    return 0.0 if ($tf <= 0);
    return 0.0 if ($df >= 1);
    return $tf * -log(0.01) if ($df <= 0);
    return $tf * -log($df);
}

sub auto_scale_size {
    my $self = shift;
    my ($fmin, $fmax) = ($self->font_size_min(), $self->font_size_max());
    my $tmin = 0;
    my $tmax = -1;    
    foreach my $c (values %{$self->{_terms}}) {        
        my $cc = $c->{TFIDF};
        next if ($cc <= 0);
        if ($tmax < $tmin) {
            $tmax = $cc; $tmin = $cc;
        }
        $tmax = $cc if ($cc > $tmax);
        $tmin = $cc if ($cc < $tmin);
    }
    foreach my $t (keys %{$self->{_terms}}) {
        my $c = $self->{_terms}{$t}{TFIDF};
        if ($c <= 0) {
            $self->{_terms}{$t}{SIZE} = 0.0;
        } elsif ($c == $tmin) {
            $self->{_terms}{$t}{SIZE} = $fmin;
        } else {
            $self->{_terms}{$t}{SIZE} = $fmin + 
                ((($fmax - $fmin)* ($c - $tmin)) / 
                 ($tmax - $tmin));
        }
    }    
}

sub get_text_cloud {
    my $self = shift;
    my ($size) = @_;
    my @toret = ();

    my $all_size = 1;
    foreach my $k (keys %{$self->{_terms}}) {
        $all_size = 0 unless (exists $self->{_terms}{$k}{SIZE});
    }
    my $sfield = 'TFIDF';
    $sfield = 'SIZE' if ($all_size);

    my @order = sort {$self->{_terms}{$b}{$sfield} <=> $self->{_terms}{$a}{$sfield}} keys %{$self->{_terms}};
    
    if (defined($size) && ($size < scalar(@order))) {
        @order = @order[0..($size-1)];
    }
    my $maxtermlen = 1;
    foreach my $k (@order) {
        $maxtermlen = length($k) if (length($k) > $maxtermlen);
    }
    push @toret, sprintf("%3s %-${maxtermlen}s  %7s  %5s  %6s  %s",
                         "#", "Term", "TF", "DF", "TFIDF", "Other");
    push @toret, "-" x (3+1+$maxtermlen+2+7+2+5+2+6+2+5);
    my $i = 0;
    foreach my $k (@order) {
        $i++;
        my $e = $self->{_terms}{$k};
        my @o = ();
        foreach my $f (sort keys %{$e}) {
            next if (($f eq "TF") || ($f eq "DF") ||
                     ($f eq "TERM") || ($f eq "TFIDF"));
            push @o, sprintf("%s=%s", $f, $e->{$f});
        }
        push @toret, sprintf("%3d %-${maxtermlen}s  %7.2f  %5.3f  %6.2f  %s",
            $i, $k, $e->{TF}, $e->{DF}, $e->{TFIDF}, join(", ", @o));
    }

    return join("\n", @toret);  
}

sub get_html_cloud {
    my $self = shift;
    my ($size) = @_;
    my @toret = ("<center><div align=center; id='jscloud'><div> ");

    my $all_size = 1;
    foreach my $k (keys %{$self->{_terms}}) {
        $all_size = 0 unless (exists $self->{_terms}{$k}{SIZE});
    }
    my $sfield = 'TFIDF';
    $sfield = 'SIZE' if ($all_size);

    my @order = sort {$self->{_terms}{$b}{$sfield} <=> $self->{_terms}{$a}{$sfield}} keys %{$self->{_terms}};
    if (defined($size) && ($size < scalar(@order))) {
        @order = @order[0..($size-1)];
    }
    my @s1 = ();
    my @s2 = ();
    foreach my $i (0..$#order) {
        if ($i % 2) {
            push @s1, $order[$i];
        } else {
            push @s2, $order[$i];
        }
    }
    @s1 = reverse(@s1);
    my @norder = (@s1, @s2);
    my $pvoff = -99999;
    foreach my $t (@norder) {
        my $e = $self->{_terms}{$t};
        next if (exists($e->{SIZE}) && $e->{SIZE} <= 0.0);
        my $tvoff = (rand(1000)/20) - 25;
        while (abs($tvoff - $pvoff) < 15) {
            $tvoff = (rand(1000)/20) - 25;
        }
        my $rw = $e->{TERM};
        $rw =~ s/_/ /g;
        my $tcolor = (exists($e->{COLOR}) ? $e->{COLOR} : $self->default_color());
        my $tsize = (exists($e->{SIZE}) ? $e->{SIZE} : 
                    (($self->font_size_min + $self->font_size_max) / 2.0));
        push @toret, sprintf("<nobr><a style='vertical-align: %.2f%%; color: %s; font-size:%d;' title='%s %.4f %.4f %.3f'>%s<sub>%s</sub></a></nobr> &nbsp; ",
                             $tvoff,
                             $tcolor,
                             $tsize, $rw, $e->{TF}, $e->{DF}, $e->{TFIDF},
                             $rw, (exists($e->{SUB}) ? $e->{SUB} : ""));
        $pvoff = $tvoff;
    }

    push @toret, "</div></div></center>";
    return join("\n", @toret);
}

sub html_header {
    return "<HTML><HEAD><META HTTP-EQUIV=\"content-type\" CONTENT=\"text/html; charset=utf-8\"><style type='text/css'>#jscloud div{width: 650px; text-align: center; border: black solid 3px;} img {border: black solid 3px; padding: 6px} a { text-decoration: none; vertical-align: middle} a:hover { text-decoration: underline; }</style></HEAD><BODY>\n";
}

sub html_footer {
    return "</BODY></HTML>\n";
}


1;
