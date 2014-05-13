package ast;

import util.F;

import java.util.List;

interface RichNode extends Node {

	void query(F<Node, Boolean> p, List<Node> acc);

	RichNode getParent();

	void setParent(RichNode parent);

	RichNode dup(RichNode dupParent);

}
