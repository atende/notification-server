library notification;
import "package:angular/angular.dart";
import "package:angular/animate/module.dart";
import 'package:angular/application_factory.dart';
import 'package:logging/logging.dart';
import 'package:vader/vader.dart';
import 'dart:html' as dom;
import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;
import 'package:highcharts4dart/highcharts.dart';

part 'app/http_interceptors.dart';
part 'app/AppRouter.dart';
part 'app/controller/graphics.dart';

// Controllers
part 'app/controller/config/email.dart';
part 'app/controller/config/geral.dart';


main(){
  // ConfigService Logger
  Logger.root.level = Level.FINEST;
  Logger.root.onRecord.listen((LogRecord r) { print(r.message); });
  var logger = new Logger("app");

  // Start Application

  var module = new Module()
    ..install(new AnimationModule())
    ..bind(HighChartComponent)
    ..type(RouteInitializerFn, implementedBy: AppRouter)
    ..factory(NgRoutingUsePushState,
      (_) => new NgRoutingUsePushState.value(false))
    ..bind(EmailCtl)
    ..bind(GraphicsCtrl)
    ..bind(GeralCtl);
  Injector di = applicationFactory().addModule(module).run();

  // Setup HttpInterceptors

  di.get(HttpInterceptors).add(new NotificationHttpInterceptor(logger));

  logger.finest("Application Started");
}