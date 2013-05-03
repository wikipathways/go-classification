use strict;
use warnings;

my $dir = '/home/martina/bigcat/projects/wikipathways/go-annotations/output/';
open (my $OUTPUT, '>>/home/martina/bigcat/projects/wikipathways/go-annotations/wiki.txt');
my $sum = 0;
my$count = 0;

foreach my $DIR (glob("$dir/*.txt")) 
{
    # print $DIR . "\n";
     
     open my $FILE, "<", $DIR or die "can't read open '$DIR'";
     my $countGO = 0;
     my $count = 1;
     
     while(<$FILE>)
     {
          
          
          if($count == 1) {
                 my $line = $_;
                 chomp $line;
                 
                  my @values = split(' ', $line);
                  my $pwId = substr($values[1], 6, (length($values[1])-10));
                  my $l  = scalar @values;
                  my $genecount = $values[$l-1];
                  my $link = "http://wikipathways.org/index.php/Pathway:" . $pwId;
                  my $name = "";
                  for (my $i = 2; $i <= $l-2 ; $i++) {
                       $name = $name . " " . $values[$i];
                   }
                  
                  print $OUTPUT "=== " . $name . " ===\n\n";
                  print $OUTPUT $genecount . " genes, [$link pathway] \n\n";
                  print $OUTPUT "||'''GO term'''||'''GO ID'''||'''changed'''||'''total'''||'''zcore'''||'''weighted'''||\n";
           }
           if($count > 2) {
                if($countGO < 15) {
                    my $line = $_;
                    chomp $line;
                 
                    my @values = split('\t', $line);
                    my $goid = $values[1];
                    my $term = $values[0];
                    my $changed  = $values[2];
                    my $total = $values[4];
                    my $weightedzscore = substr($values[6], 0, 5);
                    my $zscore = substr($values[5], 0, 5);
                    if($total < 2000) {
                         print $OUTPUT "||$term||$goid||$changed||$total||$zscore||$weightedzscore||\n";
                           $countGO++;
                    }
               }
            }
           $count++;
      }
      
       print $OUTPUT "\n----\n";
     
     close $FILE;
 }
 
 close ($OUTPUT); 
