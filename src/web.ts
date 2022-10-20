import { WebPlugin } from '@capacitor/core';

import type { AudioplayerPlugin } from './definitions';

export class AudioplayerWeb extends WebPlugin implements AudioplayerPlugin {
  async start(options: { url: string }): Promise<any> {
    console.log('start', options);
    return options;
  }
  async pause(): Promise<any> {
    console.log('pause', true);
    return true;
  }

  async stop(): Promise<any> {
    console.log('stop', true);
    return true;
  }

  async resume(): Promise<any> {
    console.log('resume', true);
    return true;
  }

  async echo(options: { value: string }): Promise<any> {
    console.log('ECHO', options);
    return options;
  }

  async seek(options: { seekTo: number }): Promise<any> {
    console.log('seek', options);
    return options;
  }

  async getDuration(): Promise<any> {
    console.log('getDuration');
    return { duration: 0 };
  }

  async getCurrentPosition(): Promise<any> {
    console.log('getCurrentPosition');
    return { currentPosition: 0 };
  }

  async getBufferedPosition(): Promise<any> {
    console.log('getBufferedPosition');
    return { bufferedPosition: 0 };
  }

  async isPlaying(): Promise<any> {
    console.log('getIsPlaying');
    return { isPlaying: false };
  }

  async isPaused(): Promise<any> {
    console.log('getIsPaused');
    return { isPaused: false };
  }

  async isStopped(): Promise<any> {
    console.log('getIsStopped');
    return { isStopped: false };
  }
}
