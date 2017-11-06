package net.bqc.jsdf.core.ast;

import net.bqc.jsdf.core.model.DecisionVertex;
import net.bqc.jsdf.core.model.StatementVertex;
import net.bqc.jsdf.core.model.Vertex;
import org.mozilla.javascript.ast.*;

import java.util.Stack;

public class CdfNodeVisitor implements NodeVisitor {

    private Vertex currentVertex;
    private Stack<Vertex> parentStack = new Stack<>();

    public CdfNodeVisitor(Vertex entryVertex) {
        this.currentVertex = entryVertex;
    }

    @Override
    public boolean visit(AstNode node) {
        if (node instanceof VariableDeclaration) {
            return visitVariableDeclaration((VariableDeclaration) node);
        }
        else if (node instanceof ExpressionStatement) {
            return visitExpressionStatement((ExpressionStatement) node);
        }
        else if (node instanceof ReturnStatement) {
            return visitReturnStatement((ReturnStatement) node);
        }
        else if (node instanceof IfStatement) {
            return visitIfStatement((IfStatement) node);
        }
        
        return true;
    }

    private boolean visitReturnStatement(ReturnStatement node) {
        StatementVertex statementVertex = new StatementVertex(node, Vertex.Type.RETURN_STATEMENT);
        statementVertex.setParent(parentStack.size() > 0 ? parentStack.peek() : null);
        linkVertices(this.currentVertex, statementVertex);
        this.currentVertex = statementVertex;
        return true;
    }

    private boolean visitIfStatement(IfStatement node) {
        DecisionVertex ifStatement = new DecisionVertex(node, Vertex.Type.IF_STATEMENT);
        ifStatement.setParent(parentStack.size() > 0 ? parentStack.peek() : null);
        // push if-statement to parent stack
        parentStack.push(ifStatement);

        node.getThenPart().visit(new CdfNodeVisitor(ifStatement));

        AstNode elsePart = node.getElsePart();
        if (elsePart != null) {
            elsePart.visit(new CdfNodeVisitor(ifStatement));
        }

        // pop if-statement from parent stack after being visited
        parentStack.pop();
        linkVertices(this.currentVertex, ifStatement);
        this.currentVertex = ifStatement;

        return false;
    }

    private boolean visitVariableDeclaration(VariableDeclaration node) {
        StatementVertex statementVertex = new StatementVertex(node, Vertex.Type.VARIABLE_DECLARATION);
        statementVertex.setParent(parentStack.size() > 0 ? parentStack.peek() : null);
        linkVertices(this.currentVertex, statementVertex);
        this.currentVertex = statementVertex;
        return true;
    }

    private boolean visitExpressionStatement(ExpressionStatement node) {
        StatementVertex statementVertex = new StatementVertex(node, Vertex.Type.EXPRESSION_STATEMENT);
        statementVertex.setParent(parentStack.size() > 0 ? parentStack.peek() : null);
        linkVertices(this.currentVertex, statementVertex);
        this.currentVertex = statementVertex;
        return true;
    }

    private void linkVertices(Vertex previousVertex, Vertex newVertex) {
        previousVertex.addTarget(newVertex);
    }
}
