part of notification;
@Controller(
  selector: '[config-email]',
  publishAs: 'ctrl'
)
class EmailCtl {
  String para;
  Http _http;
  EmailCtl(this._http){
    _loadConfig();
  }
  var config = {'protocol': 'SMTP', 'needAuthentication': true};
  void salvar(){
    _http.post('/rest/config/email', config);
  }
  sendTestEmail(){
    Notify notify = vaderFactory(Notify);
    if(para == null){
      notify.show("Para enviar um email de teste é necessário preencher todos os dados");
    }else{
      _http.post("/rest/notification/test-email", {"email": para}, params: {"email": para});

    }
  }
  _loadConfig(){
    _http.get('/rest/config/email').then((HttpResponse resp){
      if(resp.data != null){
        config = resp.data['data'][0];
      }
    });
  }
}