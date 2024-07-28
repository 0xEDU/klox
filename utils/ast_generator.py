#!/bin/python3
import sys

FOUR_SPACES = f"{'':4}"
EIGHT_SPACES = f"{'':8}"
TWELVE_SPACES = f"{'':12}"


def define_visitor(stream, base_name, types):
    stream.write(f"{FOUR_SPACES}interface Visitor<R> {{\n")
    for type_name in types.keys():
        stream.write(f"{EIGHT_SPACES}fun visit{type_name}{base_name}({base_name.lower()}: {type_name}?): R\n")
    stream.write(f"{FOUR_SPACES}}}\n")
    pass


def define_type(stream, base_name, class_name, field_list):
    stream.write(f"{FOUR_SPACES}data class {class_name} (\n")
    for field in field_list:
        stream.write(f"{EIGHT_SPACES}val {field},\n")
    stream.write(f"{FOUR_SPACES}) : {base_name}() {{\n")
    stream.write(f"{EIGHT_SPACES}override fun <R> accept(visitor: Visitor<R>): R = visitor.visit{class_name}{base_name}(this) \n")
    stream.write(f"{FOUR_SPACES}}}\n")


def define_ast(output_dir, base_name, types):
    def write_package():
        if base_name == "Expr":
            stream.write("package ft.etachott.expression\n\n")
        else:
            stream.write("package ft.etachott.statement\n\n")
            stream.write("import ft.etachott.expression.Expr\n")

    if base_name == "Expr":
        output_dir += "expression"
    else:
        output_dir += "statement"
    path = f"{output_dir}/{base_name}.kt"
    with open(path, "w") as stream:
        write_package()
        stream.write("import ft.etachott.tokens.Token\n\n")
        stream.write(f"sealed class {base_name} {{\n")
        define_visitor(stream, base_name, types)
        for k, v in types.items():
            define_type(stream, base_name, k, v)
        stream.write(f"\n{FOUR_SPACES}abstract fun <R> accept(visitor: Visitor<R>): R\n")
        stream.write("}")


def run():
    if len(sys.argv) != 2:
        print("Usage: ./ast_generator.py <output directory>")
        sys.exit(1)
    output_dir = sys.argv[1]
    expr_types = {
        "Assign": ["name: Token", "value: Expr?"],
        "Binary": ["left: Expr?", "operator: Token", "right: Expr?"],
        "Grouping": ["expression: Expr?"],
        "Literal": ["value: Any?"],
        "Unary": ["operator: Token", "right: Expr?"],
        "Variable": ["name: Token"],
    }
    define_ast(output_dir, "Expr", expr_types)
    stmt_types = {
        "Expression": ["expression: Expr?"],
        "Print": ["expression: Expr"],
        "Let": ["name: Token", "initializer: Expr?"],
    }
    define_ast(output_dir, "Stmt", stmt_types)


if __name__ == '__main__':
    run()
