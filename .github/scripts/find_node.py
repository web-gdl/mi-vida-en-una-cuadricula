"""Busca un elemento en un volcado de uiautomator y escribe "x y" de su centro.

Uso: find_node.py ui.xml "texto"   -> nodo cuyo text o content-desc contiene el texto
     find_node.py ui.xml @edit      -> primer campo de texto editable
"""
import re
import sys
import xml.etree.ElementTree as ET

path, needle = sys.argv[1], sys.argv[2]
nodes = list(ET.parse(path).getroot().iter("node"))


def center(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds", "")))
    return (x1 + x2) // 2, (y1 + y2) // 2


match = None
if needle == "@edit":
    match = next((n for n in nodes if n.get("class", "").endswith("EditText")), None)
else:
    exact = [n for n in nodes if needle in (n.get("text"), n.get("content-desc"))]
    partial = [n for n in nodes if needle in (n.get("text") or "") or needle in (n.get("content-desc") or "")]
    match = (exact or partial or [None])[0]

if match is not None:
    print("%d %d" % center(match))
