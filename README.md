metodos-mapreduce
=================

Opciones comunes para configurar Hadoop localmente:


$HADOOP_HOME/conf/mapred-site.xml
```
<configuration>
  <property>
    <name>mapred.job.tracker</name>
    <value>localhost:9001</value>
  </property>
</configuration>
```


$HADOOP_HOME/conf/hdfs-site.xml 
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


$HADOOP_HOME/conf/core-site.xml
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

### Utilizar en elstic-map-reduce

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

Crear un bucket utilizando la consola de S3 y reemplazar `[NOMBRE_DE_UN_BUCKET]` con el nombre del bucket recien creado.

```
elastic-mapreduce --create --name "mapreduce" --enable-debugging --jar s3n://metodos/mapreduce-1.0.0-SNAPSHOT.jar \
--main-class mx.itam.metodos.mr.CountNGrams \
--arg -libjars --arg s3n://metodos/guava-13.0.1.jar,s3n://metodos/lucene-analyzers-common-4.1.0.jar,s3n://metodos/lucene-core-4.1.0.jar \
--args s3n://metodos/long_abstracts_en.txt,s3n://[NOMBRE_DE_UN_BUCKET]/long_abstracts_en-out,3,true \
--num-instances 8 --instance-type m1.medium
```

Utilizar PIG para analizar los resultados

```
elastic-mapreduce --create --alive --name "Contar NGramas" --hadoop-version 1.0.3  --ami-version 2.2 \
--num-instances 8 --instance-type m1.medium --pig-interactive --pig-versions latest
```
