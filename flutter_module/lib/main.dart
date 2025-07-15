import 'package:flutter/material.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) =>
      MaterialApp(home: Scaffold(body: CardList()));
}

class CardList extends StatelessWidget {
  final List<int> numbers = List.generate(31, (index) => index + 1);

  @override
  Widget build(BuildContext context) {
    return NotificationListener<ScrollNotification>(
        onNotification: (scrollNotification) {
          // Log the type of notification
          print('ScrollNotification: ${scrollNotification.runtimeType}');
          // Log the scroll metrics
          if (scrollNotification is ScrollUpdateNotification) {
            // Specific details for scroll updates
            print('  - Pixels: ${scrollNotification.metrics.pixels.toStringAsFixed(2)}');
            print('  - Scroll Delta: ${scrollNotification.scrollDelta?.toStringAsFixed(2)}');
          } else if (scrollNotification is ScrollEndNotification) {
            // Specific details for scroll end
            print('  - Drag Details: ${scrollNotification.dragDetails}');
        }
        // Return false to allow the notification to continue to bubble up.
        // Returning true would stop it here.
        return false;
    },
    child: ListView.builder(
      hitTestBehavior: HitTestBehavior.translucent,
      padding: EdgeInsets.all(16.0),
      itemCount: numbers.length,
      itemBuilder: (BuildContext context, int index) {
        final number = numbers[index];

        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 8.0),
          child: Card(
            elevation: 4.0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(15.0),
            ),
            child: Container(
              height: 100,
              width: double.infinity,
              alignment: Alignment.center,
              child: Text(
                '$number',
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
            ),
          ),
        );
      },
    ));
  }
}