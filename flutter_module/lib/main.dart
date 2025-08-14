import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Import for MethodChannel

void main() => runApp(const MyApp());

// Enum to represent the scroll state
enum ScrollPositionState {
  atTop,
  atMiddle,
  atBottom,
}

// Helper to convert enum to string for MethodChannel
String scrollPositionStateToString(ScrollPositionState state) {
  return state.toString().split('.').last;
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) =>
      MaterialApp(home: Scaffold(body: CardList()));
}

class CardList extends StatefulWidget {
  const CardList({super.key});

  @override
  State<CardList> createState() => _CardListState();
}

class _CardListState extends State<CardList> {
  final List<int> numbers = List.generate(31, (index) => index + 1);
  ScrollPositionState _scrollPositionState = ScrollPositionState.atTop;
  final double _scrollThreshold = 5.0;

  // Define the MethodChannels
  // Flutter -> Native
  static const platform = MethodChannel('com.example.testaddtoapp/scrollposition'); // Use a unique name

  // Function to send scroll state to native
  Future<void> _sendScrollStateToNative(ScrollPositionState state) async {
    try {
      final String stateString = scrollPositionStateToString(state);
      await platform.invokeMethod('scrollStateChanged', {'state': stateString});
      print('Sent scroll state to native: $stateString');
    } on PlatformException catch (e) {
      print("Failed to send scroll state: '${e.message}'.");
    }
  }

  @override
  void initState() {
    super.initState();
    // Send initial state when the widget is first built
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (mounted) { // Ensure widget is still in the tree
        _sendScrollStateToNative(_scrollPositionState);
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return NotificationListener<ScrollNotification>(
      onNotification: (scrollNotification) {
        final metrics = scrollNotification.metrics;
        ScrollPositionState newState;

        if (metrics.pixels <= metrics.minScrollExtent + _scrollThreshold &&
            metrics.axisDirection == AxisDirection.down) {
          newState = ScrollPositionState.atTop;
        } else if (metrics.pixels >= metrics.maxScrollExtent - _scrollThreshold &&
            metrics.axisDirection == AxisDirection.down) {
          newState = ScrollPositionState.atBottom;
        } else {
          newState = ScrollPositionState.atMiddle;
        }

        if (_scrollPositionState != newState) {
          setState(() {
            _scrollPositionState = newState;
          });
          print('Scroll State Changed: $_scrollPositionState');
          _sendScrollStateToNative(_scrollPositionState); // Send state to native
        }

        if (scrollNotification is ScrollUpdateNotification) {
          // print('  - Pixels: ${metrics.pixels.toStringAsFixed(2)} '
          //       'Min: ${metrics.minScrollExtent.toStringAsFixed(2)} '
          //       'Max: ${metrics.maxScrollExtent.toStringAsFixed(2)}');
        }
        return false;
      },
      child: ListView.builder(
        // ... rest of your ListView.builder code ...
        hitTestBehavior: HitTestBehavior.translucent,
        padding: const EdgeInsets.all(16.0),
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
                  style: const TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87,
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}
