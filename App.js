/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  Text,
  FlatList,
  Button,
  NativeModules,
} from 'react-native';
import {RNCamera} from 'react-native-camera';

let camera = null;

const App: () => React$Node = () => {
  const [running, setRunning] = useState(false);
  const [data, setData] = useState(null);

  const takePicture = async () => {
    if (camera) {
      const pictureData = await camera.takePictureAsync({
        quality: 1,
      });
      return pictureData;
    }
    return null;
  };

  useEffect(() => {
    const processImage = async () => {
      if (!running) {
        return;
      }
      const pictureData = await takePicture();
      console.log('Data:', pictureData);
      if (pictureData) {
        const result = await NativeModules.ImageClassification.process(
          pictureData.uri,
        );
        console.log('Processed data: ', result);
        const temporaryData = [
          {title: 'cat', confidence: 0.9},
          {title: 'dog', confidence: 0.6},
          {title: 'window', confidence: 0.3},
        ];
        setData(temporaryData);
      }
    };

    processImage();
  }, [running, data]);

  return (
    <SafeAreaView style={styles.safeAreaView}>
      <FlatList
        style={styles.list}
        data={data}
        renderItem={({item}) => (
          <Text style={styles.row}>
            {item.label} {item.confidence.toString()}
          </Text>
        )}
        keyExtractor={(_, index) => index.toString()}
      />
      <RNCamera
        ref={ref => (camera = ref)}
        style={styles.cameraPreview}
        captureAudio={false}
      />
      <Button
        title={running ? 'Stop' : 'Start'}
        onPress={() => setRunning(!running)}
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  safeAreaView: {
    flex: 1,
    backgroundColor: 'white',
  },
  row: {
    fontSize: 15,
    color: 'black',
    textAlign: 'center',
  },
  cameraPreview: {
    flex: 8,
  },
  list: {
    flex: 1,
    paddingVertical: 5,
  },
});

export default App;
