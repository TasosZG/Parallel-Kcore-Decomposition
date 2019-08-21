#!/bin/bash
input="$1"
middle="$input.mid"
output="$2"
while read col1 col2
do 
  if [ $col1 -gt $col2 ]
  then
     echo -e "$col2\t$col1"
  else
     echo -e "$col1\t$col2"
  fi
done < "$input" > "$middle"

sort -g "$middle" | uniq > "$output"
rm $middle
