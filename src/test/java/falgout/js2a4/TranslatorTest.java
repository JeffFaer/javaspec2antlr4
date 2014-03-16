package falgout.js2a4;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.junit.Test;

public class TranslatorTest {
    @Test
    public void translationCanParseJDK() throws IOException, InterruptedException {
        ClassLoader cl = getClass().getClassLoader();
        
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try (ZipInputStream src = new ZipInputStream(cl.getResourceAsStream("src.zip"))) {
            ExecutorCompletionService<Void> runner = new ExecutorCompletionService<>(executor);
            
            final AtomicInteger total = new AtomicInteger();
            final AtomicInteger complete = new AtomicInteger();
            final AtomicLong totalTime = new AtomicLong();
            
            Map<Future<Void>, String> tasks = new LinkedHashMap<>();
            final Set<String> processing = new CopyOnWriteArraySet<>();
            
            byte[] buf = new byte[1024 * 8];
            ZipEntry e;
            while ((e = src.getNextEntry()) != null) {
                final String name = e.getName();
                if (name.endsWith(".java")) {
                    ByteArrayOutputStream sink = new ByteArrayOutputStream((int) e.getSize());
                    int read;
                    while ((read = src.read(buf)) > 0) {
                        sink.write(buf, 0, read);
                    }
                    final String file = new String(sink.toByteArray());
                    tasks.put(runner.submit(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            JavaLexer lex = new JavaLexer(new ANTLRInputStream(file));
                            JavaParser3 parse = new JavaParser3(new CommonTokenStream(lex));
                            
                            parse.getInterpreter().setPredictionMode(PredictionMode.SLL);
                            parse.setErrorHandler(new BailErrorStrategy());
                            
                            processing.add(name);
                            long nanos = System.nanoTime();
                            parse.compilationUnit();
                            nanos = System.nanoTime() - nanos;
                            processing.remove(name);
                            totalTime.addAndGet(nanos);
                            int c = complete.incrementAndGet();
                            int t = total.get();
                            double percent = 100 * c / (double) t;
                            double seconds = nanos / 10e9;
                            System.out.printf("Done (%d/%d : %.2f%%) %.2f seconds %s\n", c, t, percent, seconds, name);
                            return null;
                        }
                    }), name);
                    total.incrementAndGet();
                }
            }
            
            List<String> failed = new ArrayList<>();
            int taken = 0;
            while (taken < total.get()) {
                Future<Void> f = runner.take();
                taken++;
                try {
                    f.get();
                } catch (ExecutionException e1) {
                    failed.add(tasks.get(f));
                    System.err.println("Failed " + tasks.get(f));
                }
            }
            
            System.out.printf("Done in %.2f seconds\n", totalTime.get() / 10e9);
            
            if (failed.size() > 0) {
                System.out.println(failed);
                fail(failed.toString());
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
