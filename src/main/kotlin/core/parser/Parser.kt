package org.kkotlyarenko.core.parser

import org.kkotlyarenko.core.lexer.Token
import org.kkotlyarenko.core.lexer.TokenType
import org.kkotlyarenko.core.parser.ast.*

class Parser(tokens: Iterable<Token>) {
    private val tokens: List<Token> = tokens.toList()
    private var position: Int = 0

    fun parse(): MutableList<Statement> {
        val statements = mutableListOf<Statement>()
        while (!isAtEnd()) {
            statements.add(parseDeclaration())
        }
        return statements
    }

    private fun parseDeclaration(): Statement {
        if (match(TokenType.VAR)) return parseVarDeclaration()
        return parseStatement()
    }

    private fun parseStatement(): Statement {
        if (match(TokenType.IF)) return parseIfStatement()
        if (match(TokenType.WHILE)) return parseWhileStatement()
        if (match(TokenType.PRINT)) return parsePrintStatement()
        if (match(TokenType.LBRACE)) return BlockStatement(parseBlock())
        return parseExpressionStatement()
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(TokenType.ID, "Expected variable name.")
        var initializer: Expression? = null

        if (match(TokenType.EQ)) {
            initializer = parseExpression()
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.")
        return VarStatement(name.value, initializer)
    }

    private fun parseIfStatement(): Statement {
        consume(TokenType.LPAREN, "Expected '(' after 'if'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after 'if' condition.")

        val thenBranch = parseStatement()
        var elseBranch: Statement? = null

        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement()
        }

        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(TokenType.LPAREN, "Expected '(' after 'while'.")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expected ')' after 'while' condition.")

        val body = parseStatement()
        return WhileStatement(condition, body)
    }

    private fun parsePrintStatement(): Statement {
        val value = parseExpression()
        consume(TokenType.SEMICOLON, "Expected ';' after value.")
        return PrintStatement(value)
    }

    private fun parseExpressionStatement(): Statement {
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Expected ';' after expression.")
        return ExpressionStatement(expr)
    }

    private fun parseBlock(): MutableList<Statement> {
        val statements = mutableListOf<Statement>()

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration())
        }

        consume(TokenType.RBRACE, "Expected '}' after block.")
        return statements
    }

    private fun parseExpression(): Expression = parseAssignment()

    private fun parseAssignment(): Expression {
        val expr = parseLogicalOr()

        if (match(TokenType.EQ)) {
            val equals = previous()
            val value = parseAssignment()

            if (expr is VariableExpression) {
                return AssignExpression(expr.name, value)
            }

            throw Exception("[Parser Error] Line ${equals.line}: Invalid assignment target.")
        }

        return expr
    }

    private fun parseLogicalOr(): Expression {
        var expr = parseLogicalAnd()

        while (match(TokenType.OR)) {
            val op = previous().type
            val right = parseLogicalAnd()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseLogicalAnd(): Expression {
        var expr = parseEquality()

        while (match(TokenType.AND)) {
            val op = previous().type
            val right = parseEquality()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseEquality(): Expression {
        var expr = parseComparison()

        while (match(TokenType.EQEQ, TokenType.NEQ)) {
            val op = previous().type
            val right = parseComparison()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseComparison(): Expression {
        var expr = parseTerm()

        while (match(TokenType.LT, TokenType.LTEQ, TokenType.GT, TokenType.GTEQ)) {
            val op = previous().type
            val right = parseTerm()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseTerm(): Expression {
        var expr = parseFactor()

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            val op = previous().type
            val right = parseFactor()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseFactor(): Expression {
        var expr = parseUnary()

        while (match(TokenType.STAR, TokenType.SLASH)) {
            val op = previous().type
            val right = parseUnary()
            expr = BinaryExpression(expr, op, right)
        }

        return expr
    }

    private fun parseUnary(): Expression {
        if (match(TokenType.EXCL, TokenType.MINUS)) {
            val op = previous().type
            val right = parseUnary()
            return UnaryExpression(op, right)
        }

        return parsePrimary()
    }

    private fun parsePrimary(): Expression {
        if (match(TokenType.NUMBER)) {
            val value = previous().value.toDouble()
            return NumberExpression(value)
        }

        if (match(TokenType.ID)) {
            return VariableExpression(previous().value)
        }

        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Expected ')' after expression.")
            return expr
        }

        throw Exception("[Parser Error] Line ${peek().line}, Col ${peek().column}: Expected expression.")
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) position++
        return previous()
    }

    private fun isAtEnd(): Boolean = peek().type == TokenType.EOF

    private fun peek(): Token = tokens[position]

    private fun previous(): Token = tokens[position - 1]

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        val token = peek()
        throw Exception("[Parser Error] Line ${token.line}, Col ${token.column}: $message")
    }
}
