package org.nuthatchery.reasoning;

import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.Util;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;

public class StringLessThan extends BaseBuiltin {

	@Override
	public String getName() {
		return "StringLessThan";
	}

	@Override
	public int getArgLength() {
		return 2;
	}

	@Override
	public void headAction(Node[] args, int length, RuleContext context) {
		doUserRequiredAction(args, length, context);
	}

	@Override
	public boolean bodyCall(Node[] args, int length, RuleContext context) {
		return doUserRequiredAction(args, length, context);
	}

	private boolean doUserRequiredAction(Node[] args, int length, RuleContext context) {

		// Check we received the correct number of parameters
		checkArgs(length, context);

		boolean lessthan = false;

		// Retrieve the input arguments
		Node x = getArg(0, args, context);
		Node y = getArg(1, args, context);

		// Verify the typing of the parameters
		if (x.isLiteral() && y.isLiteral()) {
			String stringX = x.getLiteralLexicalForm();
			String stringY = y.getLiteralLexicalForm();
			lessthan = String.CASE_INSENSITIVE_ORDER.compare(stringX, stringY) < 0;
		}
		return lessthan;
	}

}
