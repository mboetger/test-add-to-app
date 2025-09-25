import 'package:flutter/material.dart';
import 'dart:math';

const String text = '''
Hello world.

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vestibulum facilisis vel quam nec scelerisque. Nullam leo sapien, ornare blandit dui ac, varius condimentum leo. Vestibulum quis sem vulputate, varius dui nec, malesuada sem. Aliquam tincidunt pretium dolor, quis ullamcorper nunc consequat quis. Donec at dui in ex pharetra pretium. Quisque molestie massa vel tellus scelerisque feugiat. Ut sed consectetur neque.''';

void main() {
  final random = Random();
  // nextInt(4) generates a number from 0-3. Add 1 to get a range of 1-4.
  final numTexts = random.nextInt(4) + 1;
  runApp(Text(text * numTexts, textDirection: TextDirection.ltr));
}
