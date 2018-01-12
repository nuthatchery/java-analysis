package org.nuthatchery.analysis.java.extractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.nuthatchery.analysis.java.extractor.FactsDb.IFactsWriter;
import org.objectweb.asm.ClassReader;

public class ExtractApi {
	private static PrintWriter output;

	public static void main(String[] args) throws IOException {
		try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			output = new PrintWriter(new OutputStreamWriter(stream, Charset.forName("UTF-8")));
			IFactsWriter fw = FactsDb.nTripleFactsWriter("/tmp/data.n3", "C");
			ClassFactExtractor ea = new ClassFactExtractor(fw, JavaUtil.stdLogger);
			ClassReader cr = new ClassReader(ExtractApi.class.getResourceAsStream("../../../../../ImmutablePosition.class"));
			cr.accept(ea, ClassReader.EXPAND_FRAMES);
			cr = new ClassReader(ExtractApi.class.getResourceAsStream("../../../../../MutablePosition.class"));
			cr.accept(ea, ClassReader.EXPAND_FRAMES);
			cr = new ClassReader(ExtractApi.class.getResourceAsStream("../../../../../Test.class"));
			cr.accept(ea, ClassReader.EXPAND_FRAMES);
			// cr = new
			// ClassReader(ExtractApi.class.getResourceAsStream("ExtractApi.class"));
			// cr.accept(ea, 0);
			fw.save();
		}
	}

}
