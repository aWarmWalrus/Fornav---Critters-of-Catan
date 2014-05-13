package ast;

import java.util.List;
import java.util.Map;

abstract class AbstractNode implements RichNode {

	private RichNode parent;

	@Override
	public RichNode getParent() {
		return parent;
	}

	@Override
	public void setParent(RichNode parent) {
		this.parent = parent;
	}

	<T extends Node> List<T> alikes(
			Map<Class<? extends Node>, Class<? extends Node>> categories) {
		return Mutation.alikes(this, categories);
	}

	<T extends Node> T alike(
			Map<Class<? extends Node>, Class<? extends Node>> categories) {
		return Mutation.alike(this, categories);
	}

}
