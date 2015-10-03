package com.JA.bletemperature.vspservice;

import java.util.LinkedList;
import java.util.Queue;

import com.JA.bletemperature.misc.DebugWrapper;
import com.JA.bletemperature.misc.StringHandler;

public class FifoQueue {
    /*
     * *************************************
     * private variables
     * *************************************
     */
    private Queue<Character> mBufferData = new LinkedList<Character>();
    private int mMaxDataToBeReadFromBuffer;
    
    /*
     * *************************************
     * constructor
     * *************************************
     */
    public FifoQueue(int maxDataToBeReadFromBuffer){
        mMaxDataToBeReadFromBuffer = maxDataToBeReadFromBuffer;
    }
    
    /*
     * *************************************
     * getter methods
     * *************************************
     */    
    public Queue<Character> getBufferData() {
        return mBufferData;
    }
    
    public int getMaxDataToBeReadFromBuffer() {
        return mMaxDataToBeReadFromBuffer;
    }
    
    /*
     * *************************************
     * private methods
     * *************************************
     */
    
    /**
     * reads the data from the buffer. The data read gets removed.
     * @return all the data from the buffer
     */
    private String getDataFromBuffer(){
        int bufferDataSize = mBufferData.size();
        StringBuilder temp = new StringBuilder();
        
        for (int i = 0; i < bufferDataSize; i++) {
            temp.append(mBufferData.remove());
        }
        return temp.toString();
    }
    
    /**
     * reads the data from the buffer index 0 to the given end index. The data read gets removed from the buffer.
     * @return the data retrieved from the buffer
     */
    private String getDataFromBuffer(int end){
        StringBuilder temp = new StringBuilder();
        
        for (int i = 0; i < end; i++) {
            temp.append(mBufferData.remove());
        }
        return temp.toString();
    }
        
    /*
     * *************************************
     * public methods
     * *************************************
     */
    /**
     * clears the whole buffer
     */
    public void flush(){
        mBufferData.clear();
    }
    
    /**
     *  Add new data in buffer, the data will be kept in the buffer 
     *  and get merged with new data whenever this method is called
     */
    public void write(String value){
        for(int i=0; i<value.length(); i++){
            mBufferData.add(value.charAt(i));
        }
    }
    
    /**
     * prepares the data that will be read next from the buffer,
     * it will read the maximum data which is defined by mMaxDataToBeReadFromFIFO
     */
    public String read(){
        DebugWrapper.infoMsg("Current data size in buffer left: " + mBufferData.size(), VSPManager.TAG);
        String queue = new String();
        
        if(mBufferData.size() <= mMaxDataToBeReadFromBuffer){
            /*
             *  we have less or equal to mMaxDataToBeReadFromFIFO bytes of data in the buffer,
             *  so we move all the available data in the sending queue
             */
            queue = getDataFromBuffer();
        } else{
            /*
             *  from the buffer(mBufferData) add the maximum allowed
             *  data (as defined by the mMaxDataToBeReadFromFIFO variable)
             *  in the sending queue
             */
            queue = getDataFromBuffer(mMaxDataToBeReadFromBuffer);

        }
        DebugWrapper.infoMsg("Queue data size to be currently send: " + queue.toString().length(), VSPManager.TAG);
        DebugWrapper.infoMsg(StringHandler.printWithNonPrintableChars("Queue data to be currently send: " + queue.toString()), VSPManager.TAG);
        

        return queue.toString();
    }
}