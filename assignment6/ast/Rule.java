package ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import util.F;
import util.Utils;

/**
 * A representation of a critter rule.
 */
public class Rule extends AbstractNode {

	Condition condition;
	List<Update> updates;
	Command must;

	public Rule(Condition condition, List<Update> updates, Command must) {
		this.condition = condition;
		this.updates = updates;
		this.must = must;
	}

	public List<Update> getUpdates(){
		return updates;
	}
	
	@Override
	public int size() {
		int size = condition.size();
		for (Update update : updates) {
			size += update.size();
		}
		size += must.size();
		return size + 1;
	}

	@Override
	public boolean mutate(Mutation.Type type) {
		RichNode parent = getParent();
		if (!(parent instanceof Program)) {
			throw new AssertionError();
		}
		Program program = (Program) parent;
		switch (type) {
		case CREATE_PARENT:
		case COPY:
			// COPY has no effect on Rules
			return false;
		case SWAP:
			if (must instanceof Action) {
				return Mutation.swap2(updates);
			} else {
				// we logically have a new candidate list with 'must' appended
				// (size := updates.size() + 1)
				int size = updates.size() + 1;
				if (size < 2) {
					return false;
				}
				List<Integer> indices = Utils.distinctRandoms(size, 2);
				Collections.sort(indices);
				int index1 = indices.get(0);
				int index2 = indices.get(1);
				if (index2 < (size - 1)) {
					Collections.swap(updates, index1, index2);
					return true;
				} else {
					Update tmp = (Update) must;
					try {
						must = updates.get(index1);
					} catch (Exception e) {
						System.out.println(updates.size());
						System.out.println(index1);
						System.out.println(index2);
						throw new RuntimeException(e);
					}
					updates.set(index1, tmp);
					return true;
				}
			}
		case REMOVE:
			boolean inRules = program.rules.remove(this);
			if (!inRules) {
				throw new AssertionError();
			}
			return true;
		case COPY_TREE:
			return Mutation.copyListedTree(this, program.rules, program);
		case REPLICATE:
			boolean excludeMust = must instanceof Action;
			if (updates.isEmpty() && excludeMust) {
				return false;
			}
			if (excludeMust) {
				Update replicate = Utils.randomlySelect(updates);
				Update replicated = replicate.dup(this);
				updates.add(replicated);
				return true;
			} else {
				Update mustUpdate = (Update) must;
				List<Update> candidates = new ArrayList<Update>();
				candidates.addAll(updates);
				candidates.add(mustUpdate);
				Update replicate = Utils.randomlySelect(candidates);
				Update replicated = replicate.dup(this);
				// we want the replicated one at the end
				must = replicated;
				updates.add(mustUpdate);
				return true;
			}
		default:
			throw new AssertionError();
		}
	}

	@Override
	public void prettyPrint(StringBuffer sb) {
//		int start = sb.length();
//		condition.prettyPrint(sb);
//		sb.append(" --> ");
//		int lastBreak = sb.lastIndexOf("\n");
//		if (lastBreak != -1) {
//			start = lastBreak + 1;// skip \n
//		}
//		int end = sb.length();
//		int off = end - start;
//		String indent = repeat(" ", off);
//		for (Update update : updates) {
//			update.prettyPrint(sb);
//			sb.append('\n');
//			sb.append(indent);
//		}
//		must.prettyPrint(sb);
//		sb.append(";");
		sb.append("|");
		condition.prettyPrint(sb);
		sb.append(" --> ");
		if (updates != null)
			for (int i = 0; i < updates.size(); i++) {
				updates.get(i).prettyPrint(sb);
				sb.append(" $ ");
			}
		must.prettyPrint(sb);
		sb.append(";");
	}

	@SuppressWarnings("unused")
	private static String repeat(String s, int n) {
		StringBuilder sb = new StringBuilder(s.length() * n);
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	@Override
	public void query(F<Node, Boolean> p, List<Node> acc) {
		if (p.f(this)) {
			acc.add(this);
		}
		condition.query(p, acc);
		for (Update update : updates) {
			update.query(p, acc);
		}
		must.query(p, acc);
	}

	@Override
	public Rule dup(RichNode dupParent) {
		Rule dup = new Rule(null, new ArrayList<Update>(), null);
		dup.setParent(dupParent);
		dup.condition = condition.dup(dup);
		for (Update update : updates) {
			dup.updates.add((Update) update.dup(dup));
		}
		dup.must = must.dup(dup);
		return dup;
	}
	
	public Condition getCondition(){
		return condition;
	}
	
	public Command getCommand(){
		return must;
	}
}
