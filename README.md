metodos-mapreduce
=================

### Calcular n-gramas dada una lista de abstracts de Wikipedia

```
Autism    Autism is a disorder of neural development characterized by impaired social interaction and communication,
and by restricted and repetitive behavior. The diagnostic criteria require that symptoms become apparent before a 
child is three years old. Autism affects information processing in the brain by altering how nerve cells and their 
synapses connect and organize; how this occurs is not well understood.

Achilles  In Greek mythology, Achilles was a Greek hero of the Trojan War, the central character and the greatest
warrior of Homer's Iliad. Plato named Achilles the most handsome of the heroes assembled against Troy. Later 
legends (beginning with a poem by Statius in the 1st century AD) state that Achilles was invulnerable in all of 
his body except for his heel. As he died because of a small wound on his heel, the term Achilles' heel has come to 
mean a person's principal weakness.
```

#### Resultados para n = 3

```
t,t,t  153925
world,war,ii	52033
administrative,district,gmina	50100
village,administrative,district	46547
u00a0,u00a0,u00a0	43681
new,york,city	41405
poland,lies,approximately	34257
register,historic,places	34149
national,register,historic	34113
during,world,war	30734
2000,socorro,linear	30548
new,south,wales	23063
2001,socorro,linear	22489
world,war,i	21159
united,states,population	20912
village,has,population	20767
major,league,baseball	20070
best,known,his	18904
he,has,been	18437
until,his,death	18364
```

### Procesar en elastic-map-reduce

Para utilizar este ejemplo es necesario crear un par de credenciales de AWS y configurar el programa elastic-mapreduce:

Descargar de from http://aws.amazon.com/developertools/2264 y configurar las credenciales.

```
  {
    "access-id": "AAAAAAAAAAAAAAAAAAAA",
    "private-key": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
    "region": "us-east-1",
    "key-pair":      "keypair",
    "key-pair-file": "/Users/user/.ssh/keypair.pem",
    "log-uri": "s3n://[NOMBRE_DE_UN_BUCKET]/logs"
  }
```

#### Calcular los n-gramas y utilizar PIG para analizar los resultados

Crear un bucket utilizando la consola de S3 y reemplazar `mi-bucket` con el nombre del bucket recien creado.

```
elastic-mapreduce --create --name "mapreduce" --enable-debugging --jar s3n://metodos/mapreduce-1.0.0-SNAPSHOT.jar \
--main-class mx.itam.metodos.mr.CountNGrams \
--arg -libjars --arg s3n://metodos/guava-13.0.1.jar,s3n://metodos/lucene-analyzers-common-4.1.0.jar,s3n://metodos/lucene-core-4.1.0.jar \
--args s3n://metodos/long_abstracts_en.txt,s3n://mi-bucket/long_abstracts_en-out,4,true \
--num-instances 8 --instance-type m1.medium
```

```
elastic-mapreduce --create --alive --name "Contar NGramas" --hadoop-version 1.0.3  --ami-version 2.2 \
--num-instances 4 --instance-type m1.medium --pig-interactive --pig-versions latest
```

```
elastic-mapreduce --jar s3://us-east-1.elasticmapreduce/libs/s3distcp/1.latest/s3distcp.jar \
--args '--src,s3://mi-bucket/long_abstracts_en.txt-out-3/,--dest,hdfs:///long_abstracts_en.txt-out-3' \
--enable-debugging --jobflow j-2DFKYG43FH7JK 
```

```
data = LOAD 'hdfs:///long_abstracts_en.txt-out-3/' AS (text:chararray, count:int);
data_sorted = ORDER data BY count DESC;
data_top = LIMIT data_sorted 1000;
STORE data_top INTO 'hdfs:///long_abstracts_en.txt-top-3/' USING PigStorage ('\t');
```

```
elastic-mapreduce --jobflow j-2DFKYG43FH7JK --jar s3://us-east-1.elasticmapreduce/libs/s3distcp/1.latest/s3distcp.jar \
--args '--dest,s3://mi-bucket/long_abstracts_en.txt-top-3/,--src,hdfs:///long_abstracts_en.txt-top-3' 
```

### Opciones comunes para configurar Hadoop localmente:

#### $HADOOP_HOME/conf/mapred-site.xml
```
<configuration>
  <property>
    <name>mapred.job.tracker</name>
    <value>localhost:9001</value>
  </property>
</configuration>
```

#### $HADOOP_HOME/conf/hdfs-site.xml 
```
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>1</value>
  </property>
  <property>
    <name>dfs.name.dir</name>
    <value>/var/data/hadoop/dfs/name</value>
  </property>
  <property>
    <name>dfs.data.dir</name>
    <value>/var/data/hadoop/dfs/data</value>
  </property>
</configuration>
```

#### $HADOOP_HOME/conf/core-site.xml
```
<configuration>
  <property>
    <name>fs.default.name</name>
    <value>hdfs://localhost:9000</value>
  </property>
  <property>
    <name>hadoop.tmp.dir</name>
    <value>/var/data/hadoop/</value>
  </property>
</configuration>
```

