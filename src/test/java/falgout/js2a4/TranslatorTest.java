package falgout.js2a4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

public class TranslatorTest {
    @Test
    public void translationCanParseJDK() throws InterruptedException, ExecutionException, IOException {
        ClassLoader cl = getClass().getClassLoader();
        
        try (ZipInputStream src = new ZipInputStream(cl.getResourceAsStream("src.zip"))) {
            ZipEntry e;
            byte[] buf = new byte[1024 * 8];
            ExecutorService runner = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            Queue<Future<String>> tasks = new ArrayDeque<>();
            
            while ((e = src.getNextEntry()) != null) {
                final String name = e.getName();
                if (name.endsWith(".java")) {
                    ByteArrayOutputStream sink = new ByteArrayOutputStream((int) e.getSize());
                    int read;
                    while ((read = src.read(buf)) > 0) {
                        sink.write(buf, 0, read);
                    }
                    final String file = new String(sink.toByteArray());
                    tasks.add(runner.submit(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            JavaLexer lex = new JavaLexer(new ANTLRInputStream(file));
                            JavaParser2 parse = new JavaParser2(new CommonTokenStream(lex));
                            parse.setErrorHandler(new BailErrorStrategy());
                            
                            System.out.println("Starting " + name);
                            
                            parse.compilationUnit();
                            return name;
                        }
                    }));
                }
            }
            int num = 0;
            while (!tasks.isEmpty()) {
                System.out.println(++num + " " + tasks.poll().get());
            }
        }
    }
}
