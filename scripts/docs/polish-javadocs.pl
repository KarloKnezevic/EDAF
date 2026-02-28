#!/usr/bin/env perl
use strict;
use warnings;

sub words_from_name {
    my ($name) = @_;
    return "value" if !defined $name || $name eq "";
    $name =~ s/_/ /g;
    $name =~ s/([a-z0-9])([A-Z])/$1 $2/g;
    $name =~ s/\s+/ /g;
    $name =~ s/^\s+|\s+$//g;
    return lc $name;
}

sub summary_description {
    my ($method_name) = @_;
    my $words = words_from_name($method_name);

    if ($method_name =~ /^create(.+)$/) {
        return "Creates " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^list(.+)$/) {
        return "Lists " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^load(.+)$/) {
        return "Loads " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^build(.+)$/) {
        return "Builds " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^parse(.+)$/) {
        return "Parses " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^validate(.+)$/) {
        return "Validates " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^compute(.+)$/) {
        return "Computes " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^update(.+)$/) {
        return "Updates " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^to(.+)$/) {
        return "Converts to " . words_from_name($1) . ".";
    }
    if ($method_name =~ /^is(.+)$/) {
        return "Checks whether " . words_from_name($1) . ".";
    }

    return "Executes " . $words . ".";
}

sub return_description {
    my ($return_type, $method_name) = @_;
    my $name_words = words_from_name($method_name);
    my $tail;

    if ($method_name =~ /^get(.+)$/) {
        return "the " . words_from_name($1);
    }
    if ($method_name =~ /^is(.+)$/) {
        return "true if " . words_from_name($1) . "; otherwise false";
    }
    if ($method_name =~ /^has(.+)$/) {
        return "true if the instance has " . words_from_name($1) . "; otherwise false";
    }
    if ($method_name =~ /^can(.+)$/) {
        return "true if the instance can " . words_from_name($1) . "; otherwise false";
    }
    if ($method_name =~ /^should(.+)$/) {
        return "true if the caller should " . words_from_name($1) . "; otherwise false";
    }
    if ($method_name =~ /^supports(.+)$/) {
        return "true if " . words_from_name($1) . " is supported; otherwise false";
    }
    if ($method_name =~ /^contains(.+)$/) {
        return "true if the container includes " . words_from_name($1) . "; otherwise false";
    }
    if ($method_name =~ /^count(.+)$/) {
        return "the number of " . words_from_name($1);
    }
    if ($method_name =~ /^size$/i) {
        return "the number of elements";
    }
    if ($method_name =~ /^to(.+)$/) {
        return "the " . words_from_name($1) . " representation";
    }
    if ($method_name =~ /^as(.+)$/) {
        return "the " . words_from_name($1) . " view";
    }
    if ($method_name =~ /^find(.+)$/) {
        return "the matching " . words_from_name($1) . ", or null if none matches";
    }

    if (defined $return_type && $return_type =~ /^(?:boolean|Boolean)$/) {
        return "true if the condition is satisfied; otherwise false";
    }
    if (defined $return_type && $return_type =~ /^(?:int|long|short|byte|double|float|Integer|Long|Short|Byte|Double|Float)$/) {
        return "the computed " . $name_words;
    }

    return "the " . $name_words . " result";
}

sub extract_method_signature {
    my ($lines_ref, $start_idx) = @_;
    my @lines = @{$lines_ref};
    my $sig = "";
    for (my $j = $start_idx; $j < @lines && $j < $start_idx + 18; $j++) {
        my $line = $lines[$j];
        next if $line =~ /^\s*$/;
        next if $line =~ /^\s*\@/;
        next if $line =~ /^\s*\/\/.*$/;
        $line =~ s/\/\/.*$//;
        $sig .= " " . $line;
        last if $line =~ /\{|;|\)\s*$/;
    }
    $sig =~ s/\s+/ /g;
    $sig =~ s/^\s+|\s+$//g;
    return $sig;
}

sub extract_method_info {
    my ($sig) = @_;
    return (undef, undef) if !defined $sig || $sig eq "";
    return (undef, undef) if $sig !~ /([A-Za-z_]\w*)\s*\(/;

    my $method_name = $1;
    my $prefix = $`;
    $prefix =~ s/<[^<>]*>/ /g;
    my @tokens = grep { $_ ne "" }
      split(/\s+/, $prefix);
    my %skip = map { $_ => 1 }
      qw(public protected private static final abstract default synchronized native strictfp);
    @tokens = grep { !$skip{$_} } @tokens;
    my $return_type = @tokens ? $tokens[-1] : undef;
    return ($return_type, $method_name);
}

my @files = @ARGV;
for my $file (@files) {
    open my $in, '<', $file or die "Cannot read $file: $!";
    my @lines = <$in>;
    close $in;

    my $changed = 0;
    for (my $i = 0; $i < @lines; $i++) {
        if ($lines[$i] =~ /\@return the computed value/) {
            my $sig = extract_method_signature(\@lines, $i + 1);
            my ($return_type, $method_name) = extract_method_info($sig);
            my $desc = return_description($return_type, ($method_name // 'method'));
            my $new = $lines[$i];
            $new =~ s/\@return the computed value/\@return $desc/;
            if ($new ne $lines[$i]) {
                $lines[$i] = $new;
                $changed = 1;
            }
        }

        if ($lines[$i] =~ /(\s*\*\s*)Executes\s+([A-Za-z0-9_]+)\s+operation\./) {
            my $prefix = $1;
            my $method_name = $2;
            $lines[$i] = $prefix . summary_description($method_name) . "\n";
            $changed = 1;
        }

        if ($lines[$i] =~ /\@return the ([a-z0-9 ]+) result/) {
            my $fragment = $1;
            my $new_fragment = $fragment;
            $new_fragment =~ s/\s+/ /g;
            $new_fragment =~ s/^\s+|\s+$//g;
            $lines[$i] =~ s/\@return the [a-z0-9 ]+ result/\@return the $new_fragment/;
            $changed = 1;
        }

        if ($lines[$i] =~ /\@param\s+([A-Za-z0-9_]+)\s+the input value(?:\s+\w+)?/) {
            my $param = $1;
            my $desc = "the " . words_from_name($param) . " argument";
            my $new = $lines[$i];
            $new =~ s/\@param\s+$param\s+the input value(?:\s+\w+)?/\@param $param $desc/;
            if ($new ne $lines[$i]) {
                $lines[$i] = $new;
                $changed = 1;
            }
        }
    }

    if ($changed) {
        open my $out, '>', $file or die "Cannot write $file: $!";
        print {$out} @lines;
        close $out;
    }
}
