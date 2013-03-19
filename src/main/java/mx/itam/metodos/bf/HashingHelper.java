package mx.itam.metodos.bf;

import java.util.Random;

import org.apache.log4j.Logger;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashingHelper {
  
  private final Logger logger = Logger.getLogger(HashingHelper.class);

  private final HashFunction[] functions;
  
  private final int m;
  
  public HashingHelper(int n, float p) {
    this.m = (int) Math.ceil((n * Math.log(p)) / Math.log(1.0 / (Math.pow(2.0, Math.log(2.0)))));
    int k = (int) Math.round(Math.log(2.0) * m / n);
    logger.info("n: " + n);
    logger.info("p: " + p);
    logger.info("m: " + m);
    logger.info("k: " + k);
    this.functions = new HashFunction[k];
    Random r = new Random(11);
    for (int i = 0; i < k; i++) {
      functions[i] = Hashing.murmur3_32(r.nextInt());
    }
  }
  
  public int[] getHashes(String s) {
    int[] hashes = new int[functions.length];
    for (int i = 0; i < functions.length; i++) {
      HashFunction hf = functions[i];
      int hash = hf.hashString(s).asInt();
      hashes[i] = (hash & 0x7FFFFFFF) % m;
    }
    return hashes;
  }
}
