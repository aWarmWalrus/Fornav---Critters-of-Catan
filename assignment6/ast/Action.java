package ast;

public interface Action extends Command {

	Action dup(RichNode dupParent);

}
