package ast;

/**
 * Sum type of <Update,Action>
 * 
 * @author hzuo
 */
public interface Command extends RichNode {

	Command dup(RichNode dupParent);

}
