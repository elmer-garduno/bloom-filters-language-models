package mx.itam.metodos.mr;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public final class NGramsAnalyzerMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

  private int n;

  @Override
  public void map(LongWritable id, Text value, Context context) throws IOException, InterruptedException {
    String[] values = value.toString().split("\t");
    if (values.length > 1) {
      analyze(values[1], context);
    }
  }
  
  private void analyze(String text, Context context) throws IOException, InterruptedException {
    Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
    TokenStream ts = analyzer.tokenStream("text", new StringReader(text));
    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
    Joiner joiner = Joiner.on(",");
    Text key = new Text();
    IntWritable count = new IntWritable(1);
    try {
      ts.reset();
      LinkedList<String> list = Lists.newLinkedList();
      while (ts.incrementToken()) {
        list.add(termAtt.toString());
        if (list.size() == n) {
          String ngram = joiner.join(list);
          key.set(ngram);
          context.write(key, count);
          list.removeFirst();
        }
      }
      ts.end();
    } finally {
      ts.close();
    }
  }
  
  @Override
  public void setup(Context context) {
    this.n = context.getConfiguration().getInt(CountNGrams.N, 3);
  }
}
