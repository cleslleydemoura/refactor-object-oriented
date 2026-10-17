// Melhorias progressivas: os formulários e a exclusão funcionam sem JavaScript.
const sinopse = document.querySelector('#sinopse');
const contador = document.querySelector('#sinopse-contador');
if (sinopse && contador) {
  const atualizar = () => { contador.textContent = `${sinopse.value.length}/2000`; };
  sinopse.addEventListener('input', atualizar);
  atualizar();
}
document.querySelector('[data-open-delete]')?.addEventListener('click', () => {
  const painel = document.querySelector('#confirmar-exclusao');
  painel.open = true;
  painel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  painel.querySelector('button').focus();
});
