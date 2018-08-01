#! /usr/bin/python

import instrinfo
import rdflib
import json
from rdflib import URIRef, BNode, Literal
from rdflib.namespace import RDF, RDFS, Namespace, XSD
import re

stackElt = re.compile('((?<![a-z0-9.])[a-zA-Z0-9.]*[a-zA-Z.][a-zA-Z0-9.]*(?![a-z0-9.]))')

jvm = Namespace('https://model.nuthatchery.org/jvm/')
g = rdflib.Graph()
g.bind('jvm', jvm)

stackNames = {
        'arrayref' : jvm.ArrayRef,
        'value' : jvm.Value,
        'objectref' : jvm.ObjectRef,
        'index' : jvm.Index,
        'count' : jvm.Count,
        'length' : jvm.Length,
        '[empty]' : jvm.StackTop,
        'result' : jvm.Value,
        '0.0' : Literal(0.0),
        '1.0' : Literal(1.0),
        '0.0f' : Literal(0.0),
        '1.0f' : Literal(1.0),
        '2.0f' : Literal(1.0),
}

def encode(x):
    if isinstance(x, list):
        if len(x) == 0:
            return RDF.nil
        else:
            n = BNode()
            g.add((n, RDF.first, encode(x[0])))
            g.add((n, RDF.rest, encode(x[1:])))
            return n
    elif isinstance(x, tuple) and len(x) == 2:
        n = jvm[str(x[0])]
        g.add((n,RDF.type,jvm.Variable))
        g.add((n,jvm['foo.bar.baz-a--b--c--d'],jvm[str(x[1])]))
        return n
    else:
        return Literal(x)

def decodeStack(s):
    s = re.sub(r'\[empty\]', '()', s)
    s = re.sub(r'\[No change\]', '', s)
    s = re.sub(r'\[same as for corresponding instructions\]', 'same', s)
    s = s.replace('{','[').replace('}',']')
    s = s.strip()
    n = BNode()
    if "→" in s:
        [before, after] = [x.strip() for x in s.split("→")]
        before = eval('[' + stackElt.sub('"\\1"', before) + ']')
        after = eval('[' + stackElt.sub('"\\1"', after) + ']')
        g.add((n, jvm.stackBefore, decode(before)))
        g.add((n, jvm.stackAfter, decode(after)))
    return n
    

with open('jvm-instructions.json') as fp:
    mnemonic = 0
    opcode = 1
    operands = 3
    stack = 4
    desc = 5

    db = json.load(fp)
    for instr in db[1:]:
        spec = instrinfo.printInstrInfo(g,instr)
        i = jvm[spec['mnemonic']]
        g.add((i,RDF.type,jvm.Instruction))
        g.add((i,jvm.mnemonic,Literal(spec['mnemonic'], datatype=XSD.string)))
        g.add((i,jvm.description, Literal(instr[desc], datatype=XSD.string)))
        if "-" not in instr[opcode]:
            g.add((i,jvm.opcode, Literal(instr[opcode], datatype=XSD.hexBinary)))
            g.add((i,jvm.opcodeNum, Literal(int(instr[opcode], 16), datatype=XSD.int)))
        g.add((i,jvm.params,encode(spec['params'])))
        g.add((i,jvm.stackBefore,encode(spec['stack'][0])))
        g.add((i,jvm.stackAfter,encode(spec['stack'][1])))
        g.add((i,jvm.expr,Literal(spec['expr'])))
        if spec['subInstrOf'] != None:
            subInstrOf = spec['subInstrOf']
            g.add((i,jvm.subInstrOf,jvm[subInstrOf[0]]))
            g.add((i,jvm.operandType,jvm[subInstrOf[1]]))
        if spec['sameAs'] != None:
            sameAs = spec['sameAs']
            print("sameAs: ", sameAs)
            n = BNode()
            g.add((n,jvm.instruction, jvm[sameAs[0]]))
            g.add((n,encode(sameAs[1]),Literal(sameAs[2])))
            g.add((i,jvm.sameAs,n))

with open('jvm-instructions.ttl', 'wb') as fp:
    g.serialize(fp, encoding='utf8', format='turtle')
#'mnemonic':n,
#            'parent':name,
#            'params':operands,
#            'stack':(stackBefore,stackAfter),
#            'expr':expr,
#            'subInstrOf':subInstrOf,
#            'sameAs':sameAs
