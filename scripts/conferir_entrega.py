"""Confere integridade editorial e conteúdo do ZIP; não substitui maven verify."""
import argparse
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / ".tools"))
sys.path.insert(0, str(ROOT / ".tools/doclibs"))
from pypdf import PdfReader
from docx import Document

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--somente-documentos", action="store_true",
                        help="Confere Word/PDF sem exigir o pacote ZIP e a compilação.")
    args = parser.parse_args()
    out = ROOT / "entrega"
    required = ["A Situação-Problema escolhida", "Objetivo", "Revisão de Conceito", "Metodologia", "Tempo", "Procedimento e material utilizado", "Resultado e discussão", "Nome:", "RGM:"]
    form = Document(out / "Projeto_Intervencao_Preenchido.docx")
    form_text = "\n".join(p.text for p in form.paragraphs)
    for field in required:
        if field not in form_text: raise AssertionError("Campo ausente no Word: " + field)
    if "☒ Situação-problema 3" not in form_text: raise AssertionError("Situação-Problema 3 não está marcada.")
    pdfs = sorted(out.glob("*.pdf"))
    for path in pdfs:
        document = PdfReader(path)
        if not document.pages: raise AssertionError("PDF vazio: " + path.name)
        for index, page in enumerate(document.pages, 1):
            text = page.extract_text() or ""
            if len(text.strip()) < 15: raise AssertionError(f"Página sem conteúdo textual: {path.name}, {index}")
            if "\ufffd" in text: raise AssertionError("Caractere inválido: " + path.name)
    for path in out.glob("*.docx"):
        with zipfile.ZipFile(path) as document:
            if document.testzip() is not None: raise AssertionError("Word corrompido: " + path.name)
    if args.somente_documentos:
        print(f"APROVADO: {len(pdfs)} PDFs e {len(list(out.glob('*.docx')))} arquivos Word íntegros; formulário com todos os campos.")
        return
    with zipfile.ZipFile(out / "catalogo.war") as war:
        entries = war.namelist()
        if war.testzip() is not None: raise AssertionError("WAR corrompido.")
        if any("tomcat-embed" in p or "/ecj-" in p for p in entries): raise AssertionError("Biblioteca do inicializador embutido dentro do WAR externo.")
        for expected in ["WEB-INF/web.xml", "WEB-INF/classes/br/edu/catalogo/service/ItemMidiaService.class", "WEB-INF/views/formulario.jsp"]:
            if expected not in entries: raise AssertionError("Conteúdo ausente no WAR: " + expected)
    with zipfile.ZipFile(out / "Acervo_Situacao_Problema_3_Completo.zip") as package:
        entries = package.namelist()
        if package.testzip() is not None: raise AssertionError("ZIP corrompido.")
        for forbidden in ["acervo/.git/", "acervo/.m2/", "acervo/.tools/", "acervo/.runtime/", "acervo/data/"]:
            if any(p.startswith(forbidden) for p in entries): raise AssertionError("Arquivo indevido no pacote: " + forbidden)
        for expected in ["acervo/src/main/java/br/edu/catalogo/web/ItemMidiaServlet.java", "acervo/src/test/java/br/edu/catalogo/web/CatalogoHttpIT.java",
                         "acervo/docs/javadoc/index.html", "acervo/entrega/Projeto_Intervencao_Preenchido.docx", "acervo/target/classes/br/edu/catalogo/launcher/ServidorLocal.class",
                         "acervo/target/catalogo/WEB-INF/web.xml", "acervo/target/dependency/h2-2.3.232.jar"]:
            if expected not in entries: raise AssertionError("Conteúdo ausente no pacote: " + expected)
    print(f"APROVADO: {len(pdfs)} PDFs, {len(list(out.glob('*.docx')))} arquivos Word, formulário com todos os campos, WAR e ZIP íntegros.")
    print("O pacote contém fontes, testes, documentação e compilação; exclui caches e bancos de dados.")

if __name__ == "__main__": main()
