"""Popula um catálogo local vazio através dos formulários HTTP existentes."""
import http.cookiejar
import re
import sys
import urllib.parse
import urllib.request

OBRAS = [
    ("LIVRO", "Dom Casmurro", "Machado de Assis", "1899", "Romance", "Bento Santiago revisita suas memórias e constrói um relato marcado por afetos, dúvidas e pela passagem do tempo."),
    ("FILME", "A Viagem de Chihiro", "Hayao Miyazaki", "2001", "Animação", "Ao entrar em um mundo extraordinário, uma menina precisa encontrar coragem e novos aliados para ajudar sua família."),
    ("SERIE", "Dark", "Baran bo Odar", "2017", "Ficção científica", "O desaparecimento de uma criança aproxima famílias e revela conexões inesperadas entre diferentes momentos de uma cidade."),
    ("LIVRO", "O Pequeno Príncipe", "Antoine de Saint-Exupéry", "1943", "Fábula", "O encontro entre um aviador e um viajante de outro planeta conduz a reflexões sobre amizade, cuidado e o olhar para o mundo."),
    ("FILME", "Interestelar", "Christopher Nolan", "2014", "Ficção científica", "Uma equipe parte em busca de um futuro para a humanidade enquanto seus integrantes enfrentam distância, tempo e escolhas pessoais."),
    ("SERIE", "Anne with an E", "Moira Walley-Beckett", "2017", "Drama", "Uma jovem imaginativa encontra um novo lar e transforma as relações ao seu redor enquanto busca pertencimento e oportunidades."),
]

def main():
    base = sys.argv[1].rstrip("/") if len(sys.argv) > 1 else "http://localhost:8080"
    endereco = urllib.parse.urlparse(base)
    if endereco.scheme != "http" or endereco.hostname not in {"localhost", "127.0.0.1"}:
        raise SystemExit("Use o endereço HTTP do servidor local.")
    cliente = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
    pagina = cliente.open(base + "/itens", timeout=30).read().decode("utf-8")
    if 'class="media-card"' in pagina:
        raise SystemExit("O catálogo já contém itens. A demonstração só popula um catálogo vazio.")
    formulario = cliente.open(base + "/itens/novo", timeout=30).read().decode("utf-8")
    token = re.search(r'name="csrfToken" value="([^"]+)"', formulario).group(1)
    for tipo, titulo, autor, ano, genero, sinopse in OBRAS:
        dados = urllib.parse.urlencode(dict(csrfToken=token, tipoMidia=tipo, titulo=titulo,
                   autorDiretor=autor, anoLancamento=ano, genero=genero, sinopse=sinopse)).encode()
        resposta = cliente.open(base + "/itens/salvar", data=dados, timeout=30)
        if resposta.status != 200 or "/detalhes?id=" not in resposta.url:
            raise SystemExit("Falha no cadastro de " + titulo)
    print("Seis obras demonstrativas cadastradas com sucesso.")

if __name__ == "__main__":
    main()
