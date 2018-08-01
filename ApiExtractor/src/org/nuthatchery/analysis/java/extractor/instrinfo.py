import re
import json
from rdflib import URIRef, BNode, Literal
from rdflib.namespace import RDF, RDFS, Namespace, XSD

stackElt = re.compile('((?<![a-z0-9.])[a-zA-Z0-9.]*[a-zA-Z.][a-zA-Z0-9.]*(?![a-z0-9.]))')


class Name:
    def __init__(self,name,env):
        self.name = name
        self.env = env
        self.n = env.get(name,0) + 1
        env[name] = self.n 
    
    def __str__(self):
        if self.env[self.name] == 1:
            return self.name
        else:
            return self.name + str(self.n)

def decode(g,x):
    if isinstance(x, list):
        if len(x) == 0:
            return RDF.nil
        else:
            n = BNode()
            g.add((n, RDF.first, decode(g,x[0])))
            g.add((n, RDF.rest, decode(g,x[1:])))
            return n
    else:
        return Literal(x)

def decodeStackDesc(s):
    s = re.sub(r'\[empty\]', '()', s)
    s = re.sub(r'\[No change\]', '', s)
    s = re.sub(r'\[same as for corresponding instructions\]', 'same', s)
    s = s.replace('{','[').replace('}',']')
    s = s.strip()
    if "→" in s:
        [before, after] = [x.strip() for x in s.split("→")]
        before = eval('[' + stackElt.sub('"\\1"', before) + ']')
        after = eval('[' + stackElt.sub('"\\1"', after) + ']')
        return (before,after)
    return ("",[s])


regex = re.compile('^([abcdfils])(cmp|const|add|div|inc|load|mul|and|neg|or|rem|return|shl|shr|ipush|aload|astore|store|sub|ushr|xor|throw|newarray|2)(.*)')

types = { 'a' : 'objectRef', 'b' : 'byte', 'c' : 'char', 'd' : 'double', 'f' : 'float', 'i' : 'int', 'l' : 'long', 's' : 'short' }
patterns = {
"2":"@→$",
"add":"@@→@",
"aload":"ai→@",
"and":"@@→@",
"astore":"ai@→",
"cmp":"@@→@",
"const":"C;→@",
"div":"@@→@",
"inc":"VB;→",
"ipush":"B;@→i",
"load":"V;→@",
"mul":"@@→@",
"neg":"@→@",
"or":"@@→@",
"rem":"@@→@",
"return":"@→",
"shl":"@@→@",
"shr":"@@→@",
"store":"V;@→",
"sub":"@@→@",
"ushr":"@@→@",
"xor":"@@→@",
}
instrinfo = {
          "aaload" : { "base" : "aload", "extra" : "", "type" : "a"},
  "aastore" : { "base" : "astore", "extra" : "", "type" : "a"},
  "aconst_null" : { "base" : "const", "extra" : "_null", "type" : "a"},
  "aload_0" : { "base" : "load", "extra" : "_0", "type" : "a"},
  "aload_1" : { "base" : "load", "extra" : "_1", "type" : "a"},
  "aload_2" : { "base" : "load", "extra" : "_2", "type" : "a"},
  "aload_3" : { "base" : "load", "extra" : "_3", "type" : "a"},
  "aload" : { "base" : "load", "extra" : "", "type" : "a"},
#  "anewarray" : { "base" : "newarray", "extra" : "", "type" : "a"},
  "areturn" : { "base" : "return", "extra" : "", "type" : "a"},
  "astore_0" : { "base" : "store", "extra" : "_0", "type" : "a"},
  "astore_1" : { "base" : "store", "extra" : "_1", "type" : "a"},
  "astore_2" : { "base" : "store", "extra" : "_2", "type" : "a"},
  "astore_3" : { "base" : "store", "extra" : "_3", "type" : "a"},
  "astore" : { "base" : "store", "extra" : "", "type" : "a"},
#  "athrow" : { "base" : "throw", "extra" : "", "type" : "a"},
  "baload" : { "base" : "aload", "extra" : "", "type" : "b"},
  "bastore" : { "base" : "astore", "extra" : "", "type" : "b"},
  "bipush" : { "base" : "ipush", "extra" : "", "type" : "b"},
  "caload" : { "base" : "aload", "extra" : "", "type" : "c"},
  "castore" : { "base" : "astore", "extra" : "", "type" : "c"},
  "d2f" : { "base" : "2", "extra" : "f", "type" : "d"},
  "d2i" : { "base" : "2", "extra" : "i", "type" : "d"},
  "d2l" : { "base" : "2", "extra" : "l", "type" : "d"},
  "dadd" : { "base" : "add", "extra" : "", "type" : "d"},
  "daload" : { "base" : "aload", "extra" : "", "type" : "d"},
  "dastore" : { "base" : "astore", "extra" : "", "type" : "d"},
  "dcmpg" : { "base" : "cmp", "extra" : "g", "type" : "d"},
  "dcmpl" : { "base" : "cmp", "extra" : "l", "type" : "d"},
  "dconst_0" : { "base" : "const", "extra" : "_0", "type" : "d"},
  "dconst_1" : { "base" : "const", "extra" : "_1", "type" : "d"},
  "ddiv" : { "base" : "div", "extra" : "", "type" : "d"},
  "dload_0" : { "base" : "load", "extra" : "_0", "type" : "d"},
  "dload_1" : { "base" : "load", "extra" : "_1", "type" : "d"},
  "dload_2" : { "base" : "load", "extra" : "_2", "type" : "d"},
  "dload_3" : { "base" : "load", "extra" : "_3", "type" : "d"},
  "dload" : { "base" : "load", "extra" : "", "type" : "d"},
  "dmul" : { "base" : "mul", "extra" : "", "type" : "d"},
  "dneg" : { "base" : "neg", "extra" : "", "type" : "d"},
  "drem" : { "base" : "rem", "extra" : "", "type" : "d"},
  "dreturn" : { "base" : "return", "extra" : "", "type" : "d"},
  "dstore_0" : { "base" : "store", "extra" : "_0", "type" : "d"},
  "dstore_1" : { "base" : "store", "extra" : "_1", "type" : "d"},
  "dstore_2" : { "base" : "store", "extra" : "_2", "type" : "d"},
  "dstore_3" : { "base" : "store", "extra" : "_3", "type" : "d"},
  "dstore" : { "base" : "store", "extra" : "", "type" : "d"},
  "dsub" : { "base" : "sub", "extra" : "", "type" : "d"},
  "f2d" : { "base" : "2", "extra" : "d", "type" : "f"},
  "f2i" : { "base" : "2", "extra" : "i", "type" : "f"},
  "f2l" : { "base" : "2", "extra" : "l", "type" : "f"},
  "fadd" : { "base" : "add", "extra" : "", "type" : "f"},
  "faload" : { "base" : "aload", "extra" : "", "type" : "f"},
  "fastore" : { "base" : "astore", "extra" : "", "type" : "f"},
  "fcmpg" : { "base" : "cmp", "extra" : "g", "type" : "f"},
  "fcmpl" : { "base" : "cmp", "extra" : "l", "type" : "f"},
  "fconst_0" : { "base" : "const", "extra" : "_0", "type" : "f"},
  "fconst_1" : { "base" : "const", "extra" : "_1", "type" : "f"},
  "fconst_2" : { "base" : "const", "extra" : "_2", "type" : "f"},
  "fdiv" : { "base" : "div", "extra" : "", "type" : "f"},
  "fload_0" : { "base" : "load", "extra" : "_0", "type" : "f"},
  "fload_1" : { "base" : "load", "extra" : "_1", "type" : "f"},
  "fload_2" : { "base" : "load", "extra" : "_2", "type" : "f"},
  "fload_3" : { "base" : "load", "extra" : "_3", "type" : "f"},
  "fload" : { "base" : "load", "extra" : "", "type" : "f"},
  "fmul" : { "base" : "mul", "extra" : "", "type" : "f"},
  "fneg" : { "base" : "neg", "extra" : "", "type" : "f"},
  "frem" : { "base" : "rem", "extra" : "", "type" : "f"},
  "freturn" : { "base" : "return", "extra" : "", "type" : "f"},
  "fstore_0" : { "base" : "store", "extra" : "_0", "type" : "f"},
  "fstore_1" : { "base" : "store", "extra" : "_1", "type" : "f"},
  "fstore_2" : { "base" : "store", "extra" : "_2", "type" : "f"},
  "fstore_3" : { "base" : "store", "extra" : "_3", "type" : "f"},
  "fstore" : { "base" : "store", "extra" : "", "type" : "f"},
  "fsub" : { "base" : "sub", "extra" : "", "type" : "f"},
  "i2b" : { "base" : "2", "extra" : "b", "type" : "i"},
  "i2c" : { "base" : "2", "extra" : "c", "type" : "i"},
  "i2d" : { "base" : "2", "extra" : "d", "type" : "i"},
  "i2f" : { "base" : "2", "extra" : "f", "type" : "i"},
  "i2l" : { "base" : "2", "extra" : "l", "type" : "i"},
  "i2s" : { "base" : "2", "extra" : "s", "type" : "i"},
  "iadd" : { "base" : "add", "extra" : "", "type" : "i"},
  "iaload" : { "base" : "aload", "extra" : "", "type" : "i"},
  "iand" : { "base" : "and", "extra" : "", "type" : "i"},
  "iastore" : { "base" : "astore", "extra" : "", "type" : "i"},
  "iconst_0" : { "base" : "const", "extra" : "_0", "type" : "i"},
  "iconst_1" : { "base" : "const", "extra" : "_1", "type" : "i"},
  "iconst_2" : { "base" : "const", "extra" : "_2", "type" : "i"},
  "iconst_3" : { "base" : "const", "extra" : "_3", "type" : "i"},
  "iconst_4" : { "base" : "const", "extra" : "_4", "type" : "i"},
  "iconst_5" : { "base" : "const", "extra" : "_5", "type" : "i"},
  "iconst_m1" : { "base" : "const", "extra" : "_m1", "type" : "i"},
  "idiv" : { "base" : "div", "extra" : "", "type" : "i"},
  "iinc" : { "base" : "inc", "extra" : "", "type" : "i"},
  "iload_0" : { "base" : "load", "extra" : "_0", "type" : "i"},
  "iload_1" : { "base" : "load", "extra" : "_1", "type" : "i"},
  "iload_2" : { "base" : "load", "extra" : "_2", "type" : "i"},
  "iload_3" : { "base" : "load", "extra" : "_3", "type" : "i"},
  "iload" : { "base" : "load", "extra" : "", "type" : "i"},
  "imul" : { "base" : "mul", "extra" : "", "type" : "i"},
  "ineg" : { "base" : "neg", "extra" : "", "type" : "i"},
  "ior" : { "base" : "or", "extra" : "", "type" : "i"},
  "irem" : { "base" : "rem", "extra" : "", "type" : "i"},
  "ireturn" : { "base" : "return", "extra" : "", "type" : "i"},
  "ishl" : { "base" : "shl", "extra" : "", "type" : "i"},
  "ishr" : { "base" : "shr", "extra" : "", "type" : "i"},
  "istore_0" : { "base" : "store", "extra" : "_0", "type" : "i"},
  "istore_1" : { "base" : "store", "extra" : "_1", "type" : "i"},
  "istore_2" : { "base" : "store", "extra" : "_2", "type" : "i"},
  "istore_3" : { "base" : "store", "extra" : "_3", "type" : "i"},
  "istore" : { "base" : "store", "extra" : "", "type" : "i"},
  "isub" : { "base" : "sub", "extra" : "", "type" : "i"},
  "iushr" : { "base" : "ushr", "extra" : "", "type" : "i"},
  "ixor" : { "base" : "xor", "extra" : "", "type" : "i"},
  "l2d" : { "base" : "2", "extra" : "d", "type" : "l"},
  "l2f" : { "base" : "2", "extra" : "f", "type" : "l"},
  "l2i" : { "base" : "2", "extra" : "i", "type" : "l"},
  "ladd" : { "base" : "add", "extra" : "", "type" : "l"},
  "laload" : { "base" : "aload", "extra" : "", "type" : "l"},
  "land" : { "base" : "and", "extra" : "", "type" : "l"},
  "lastore" : { "base" : "astore", "extra" : "", "type" : "l"},
  "lcmp" : { "base" : "cmp", "extra" : "", "type" : "l"},
  "lconst_0" : { "base" : "const", "extra" : "_0", "type" : "l"},
  "lconst_1" : { "base" : "const", "extra" : "_1", "type" : "l"},
  "ldiv" : { "base" : "div", "extra" : "", "type" : "l"},
  "lload_0" : { "base" : "load", "extra" : "_0", "type" : "l"},
  "lload_1" : { "base" : "load", "extra" : "_1", "type" : "l"},
  "lload_2" : { "base" : "load", "extra" : "_2", "type" : "l"},
  "lload_3" : { "base" : "load", "extra" : "_3", "type" : "l"},
  "lload" : { "base" : "load", "extra" : "", "type" : "l"},
  "lmul" : { "base" : "mul", "extra" : "", "type" : "l"},
  "lneg" : { "base" : "neg", "extra" : "", "type" : "l"},
  "lor" : { "base" : "or", "extra" : "", "type" : "l"},
  "lrem" : { "base" : "rem", "extra" : "", "type" : "l"},
  "lreturn" : { "base" : "return", "extra" : "", "type" : "l"},
  "lshl" : { "base" : "shl", "extra" : "", "type" : "l"},
  "lshr" : { "base" : "shr", "extra" : "", "type" : "l"},
  "lstore_0" : { "base" : "store", "extra" : "_0", "type" : "l"},
  "lstore_1" : { "base" : "store", "extra" : "_1", "type" : "l"},
  "lstore_2" : { "base" : "store", "extra" : "_2", "type" : "l"},
  "lstore_3" : { "base" : "store", "extra" : "_3", "type" : "l"},
  "lstore" : { "base" : "store", "extra" : "", "type" : "l"},
  "lsub" : { "base" : "sub", "extra" : "", "type" : "l"},
  "lushr" : { "base" : "ushr", "extra" : "", "type" : "l"},
  "lxor" : { "base" : "xor", "extra" : "", "type" : "l"},
  "saload" : { "base" : "aload", "extra" : "", "type" : "s"},
  "sastore" : { "base" : "astore", "extra" : "", "type" : "s"},
  "sipush" : { "base" : "ipush", "extra" : "", "type" : "s"},
}

seen = set()

def suf2opnd(suf):
    if suf == '_m1':
        return -1
    elif suf == '_null':
        return 'null'
    elif suf[1:].isnumeric():
        return int(suf[1:])
    else:
        return None

def printInstrInfo(g,instr):
    n = instr[0]
    name = n
    pre = ""
    suf = ""
    (stackBefore,stackAfter) = decodeStackDesc(instr[4])
    operands = [x.strip() for x in instr[3].split(",") if x != '']
    subInstrOf = None
    sameAs = None
    eas = []
    if n in instrinfo:
        info = instrinfo[n]
        name = info['base']
        pre = info['type']
        suf = info['extra']
        pat = patterns.get(name, '')
        nameOpnd = suf2opnd(suf)
        if True:
            print(operands,stackBefore,stackAfter)
            [opnds,pat] = pat.split(';',1) if ';' in pat else ['',pat]
            [args,rets] = pat.split('→',1)
            operands = []
            stackBefore = []
            stackAfter = []
            varName = pre if pre != 'a' else 'o'
            subInstrOf = (name,types[pre])
            env = {}
            for i in range(len(opnds)):
                o = opnds[i]
                if o == 'C':
                    operands.append((Name(varName,env),types[pre]))
                elif o == 'B':
                    operands.append((Name('c',env), 'byte'))
                elif o == 'V':
                    operands.append((Name('v',env), 'variable'))
                else:
                    raise Exception("unknown: " + o)
            assert nameOpnd == None or len(operands) > 0
            eas = [str(x[0]) for x in operands]
            if len(operands) > 0 and nameOpnd != None:
                sameAs = (pre+name, operands[0], nameOpnd)
                eas[0] = str(nameOpnd)
                del operands[0]
            v = 1
            for (ps,ss) in [(args,stackBefore), (rets,stackAfter)]:
                for i in range(len(ps)):
                    a = ps[i]
                    if a == '@':
                        ss.append((Name(varName,env), types[pre]))
                    elif a == '$':
                        ss.append((Name(suf,env), types[suf]))
                    elif a == 'a':
                        ss.append((Name('a',env), 'arrayRef'))
                    elif a == 'i':
                        ss.append((Name('i',env), 'int'))
                    else:
                        raise Exception("unknown: " + a)
                    v += 1
            eas.extend([str(x[0]) for x in stackBefore])
            rs = [str(x[0]) for x in stackAfter]
    else:
            eas.extend([str(x) for x in stackBefore])
            rs = [str(x) for x in stackAfter]
            print("rs: ", rs)
    expr = '' if len(rs) == 0 else rs[0] if len(rs) == 1 else ",".join(rs)
    expr = expr + ' = ' if expr != '' else expr
    expr = expr + name + ('_' + pre if pre != '' else '') + '(' + ", ".join(eas) + ')'

    print('%-10s = %s  [[ %s ]] %s → %s  %-20s  %-30s %s' % (n, pre + ":" + name + ":" + suf, expr, str(stackBefore), str(stackAfter), instr[3], instr[4], instr[5]))
    return {
            'mnemonic':n,
            'parent':name,
            'params':operands,
            'stack':(stackBefore,stackAfter),
            'expr':expr,
            'subInstrOf':subInstrOf,
            'sameAs':sameAs
    }
            
#        if name not in seen:
#            seen.add(name)
#            t = "@@→@" if "," in i[4] else "@→@"
#            print('%-10s  %-4s  %-15s      %-20s  %-30s %s' % (name, t, pre + ":" + name + ":" + suf, i[3], i[4], i[5]))
#        if "," in i[4]:
#            t = pre + pre + "→" + pre
#        else:
#            t = pre + "→" + pre
        #print(    '%-10s  %-4s  %-15s      %-20s  %-30s %s' % (name, t, pre + ":" + name + ":" + suf, i[3], i[4], i[5]))
    #print('"%s" : {"name":"%s", "pre":"%s", "suf":"%s", "stack":"%s", "desc":"%s", "more":"%s","code":"%s"},' % (n, name, pre, suf, i[4], i[5],i[1],i[3]))

