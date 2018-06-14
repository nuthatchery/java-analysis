module org::nuthatchery::analysis::agc::AgcSyntax

syntax AgcModule = {AgcLine "\n"}*;

syntax AgcLine 
	= EmptyLine: SS? AgcComment?
	| IncludeLine:    [$] FILENAME  SS? AgcComment?
	| LabeledLine:    AgcLabel      SS  AgcCmdLine 
	| UnlabeledLine:                SS  AgcCmdLine
	| PreCommentLine: SS AgcNonOperand SS  AgcCmdLine
	| Label FalseLabel Operator Operand Mod1 Mod2 Comment
	;
syntax AgcCmdLine
	= AgcInstruction (SS AgcOperand)* SS? AgcComment?
	;

lexical AgcComment = [#] ![\n]*;	
lexical AgcInstruction = [A-Z] ![\ \t\n#]*;
lexical AgcOperand = AgcToken;
lexical AgcNonOperand = ![A-Z\ \t\n#$] AgcToken?;
lexical AgcLabel = ![\ \t\n#$] AgcToken?;
lexical AgcToken = ![\ \t\n#]+;
lexical S = [\ \t];
lexical SS = S+ !>> [\ \t];
