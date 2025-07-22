import 'package:flutter/material.dart';

void main() => runApp(const ExampleApp());

class ExampleApp extends StatelessWidget {
  const ExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
        width: double.infinity,
        height: 300,
        color: Colors.grey,
        child: Center(
          child: Container(color: Colors.red, width: 100, height: 100),
        )
      );
  }
}