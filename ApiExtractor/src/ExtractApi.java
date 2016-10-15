import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.util.Printer;


public class ExtractApi extends ClassVisitor {

	public static void main(String[] args) throws IOException {
		ExtractApi ea = new ExtractApi();
		ClassReader cr = new ClassReader("ExtractApi");
		cr.accept(ea, 0);
	}
	public ExtractApi() {
		super(Opcodes.ASM4);
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		System.out.println(name + " extends " + superName + " {");
	}

	public void visitSource(String source, String debug) {
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	public void visitAttribute(Attribute attr) {
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		System.out.println("    " + desc + " " + name);
		return null;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		System.out.println("    " + name + desc);
		return new MethodVisitor(Opcodes.ASM4) {
			@Override public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
				
			}
			
			@Override public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				System.out.println("CALL: " + Printer.OPCODES[opcode] + " " + owner + "." + name  + " [" + desc + "]");
			}
			
			@Override public void visitTypeInsn(int opcode, String type) {
				System.out.println("TYPE: " + Printer.OPCODES[opcode] + " " + type);
			}
		};
	}

	public void visitEnd() {
		System.out.println("}");
	}
}
