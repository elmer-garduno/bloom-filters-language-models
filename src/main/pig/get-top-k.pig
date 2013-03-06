data = LOAD '$INPUT' AS (text:chararray, count:int);
data_sorted = ORDER data BY count DESC;
data_top = LIMIT data_sorted $K;
STORE data_top INTO '$OUTPUT' USING PigStorage ('\t');
