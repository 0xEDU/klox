#!/bin/python3
import sys


def define_type(stream, base_name, class_name, field_list):
    stream.write(f"{'':<4}data class {class_name} (\n")
    for field in field_list:
        stream.write(f"{'':<8}val {field},\n")
    stream.write(f"{'':<4})\n")


def define_ast(output_dir, base_name, types):
    path = f"{output_dir}/{base_name}.kt"
    with open(path, "w") as stream:
        stream.write("package ft.etachott.expression\n\n")
        stream.write("import ft.etachott.tokens.Token\n\n")
        stream.write(f"sealed class {base_name} {{\n")
        for k, v in types.items():
            define_type(stream, base_name, k, v)
        stream.write("}")


def run():
    if len(sys.argv) != 2:
        print("Usage: ./ast_generator.py <output directory>")
        sys.exit(1)
    output_dir = sys.argv[1]
    types = {
        "Binary": ["left: Expr?", "operator: Token", "right: Expr?"],
        "Grouping": ["expression: Expr?"],
        "Literal": ["value: Any?"],
        "Unary": ["operator: Token", "right: Expr?"]
    }
    define_ast(output_dir, "Expr", types)


if __name__ == '__main__':
    run()
