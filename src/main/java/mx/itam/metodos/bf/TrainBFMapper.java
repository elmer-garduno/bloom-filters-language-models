package mx.itam.metodos.bf;

import java.io.IOException;

import mx.itam.metodos.common.IntArrayWritable;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

// Based on the algorithm described on 
// Smoothed Bloom ﬁlter language models: Tera-Scale LMs on the Cheap
// David Talbot and Miles Osborne
// http://acl.ldc.upenn.edu/D/D07/D07-1049.pdf

public final class TrainBFMapper extends Mapper<LongWritable, Text, NullWritable, IntArrayWritable> {
  
  private final Logger logger = Logger.getLogger(TrainBFMapper.class);
  
  private HashingHelper functions;
  
  private int maxQCount = 0;

  @Override
  public void map(LongWritable id, Text value, Context context) throws IOException, InterruptedException {
    String[] values = value.toString().split("\t");
    int count = Integer.parseInt(values[1]);
    int qc = quantize(count);
    NullWritable key = NullWritable.get();
    IntArrayWritable out = new IntArrayWritable();
    for (int j = 0; j < qc; j++) {
      String rep = String.format("%s_%s", values[0], j);
      int[] hs = functions.getHashes(rep);
      IntWritable[] hashes = new IntWritable[hs.length];
      for (int i = 0; i < hs.length; i++) {
        hashes[i] = new IntWritable(hs[i]);
      }
      out.set(hashes);
      context.write(key, out);
      if (qc > maxQCount) {
        maxQCount = qc;
      }
    }
  }

  private int quantize(int x) {
    double log = Math.log(x) / Math.log(2);
    return 1 + (int) Math.floor(log);
  }

  @Override
  public void cleanup(Context context) {
    logger.info("MQC: " + maxQCount);
  }
  
  @Override
  public void setup(Context context) {
    int n = context.getConfiguration().getInt(BFLMDriver.N, 10000);
    float p = context.getConfiguration().getFloat(BFLMDriver.P, 0.03f);
    this.functions = new HashingHelper(n, p);
  }
}
