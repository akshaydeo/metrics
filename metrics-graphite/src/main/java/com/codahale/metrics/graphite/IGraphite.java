package com.codahale.metrics.graphite;

import java.io.IOException;

/**
 * Graphite related interface
 * Author: akshay
 * Date  : 11/21/13
 * Time  : 7:56 PM
 */
public interface IGraphite {
    /**
     * Method to connect to the carbon server
     *
     * @throws IOException
     */
    public void connect () throws IOException;

    /**
     * Method to close the connection
     *
     * @throws IOException
     */
    public void close () throws IOException;

    /**
     * Method to send data
     *
     * @param name
     * @param value
     * @param timestamp
     * @throws IOException
     */
    public void send (final String name, final String value, final long timestamp) throws IOException;
}
