import { Plugin } from "@capacitor/core";

export interface AudioplayerPlugin extends Plugin{
  start(options: { url: string }): Promise<any>;
  pause(): Promise<any>;
  stop(): Promise<any>;
  resume(): Promise<any>;
  seek(options: { seekTo: number }): Promise<any>;
  getDuration(): Promise<any>;
  getCurrentPosition(): Promise<any>;
  getBufferedPosition(): Promise<any>;
  isPlaying(): Promise<any>;
  isPaused(): Promise<any>;
  isStopped(): Promise<any>;
  echo(options: { value: string }): Promise<{ value: string }>;
}
