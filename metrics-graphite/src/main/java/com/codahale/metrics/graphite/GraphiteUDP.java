package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A client to a Carbon server.
 */
public class GraphiteUDP implements Closeable, IGraphite {
    private static final Logger logger = LoggerFactory.getLogger(GraphiteUDP.class);
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    // this may be optimistic about Carbon/Graphite
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final InetSocketAddress address;
    private final Charset charset;

    private DatagramSocket socket;
    private int failures;

    /**
     * Creates a new client which connects to the given address using the default
     * {@link javax.net.SocketFactory}.
     *
     * @param address the address of the Carbon server
     */
    public GraphiteUDP (InetSocketAddress address) {
        this.address = address;
        this.charset = UTF_8;
    }


    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address the address of the Carbon server
     * @param charset the character set used by the server
     */
    public GraphiteUDP (InetSocketAddress address, Charset charset) {
        this.address = address;
        this.charset = charset;
    }

    /**
     * Connects to the server.
     *
     * @throws IllegalStateException if the client is already connected
     * @throws java.io.IOException   if there is an error connecting
     */
    public void connect () throws IllegalStateException, IOException {
        if (socket != null) {
            logger.trace("Already connected");
            throw new IllegalStateException("Already connected");
        }
        this.socket = new DatagramSocket();
    }

    /**
     * Sends the given measurement to the server.
     *
     * @param name      the name of the metric
     * @param value     the value of the metric
     * @param timestamp the timestamp of the metric
     * @throws java.io.IOException if there was an error sending the metric
     */
    public void send (String name, String value, long timestamp) throws IOException {
        try {
            final StringBuffer packetData = new StringBuffer();
            packetData.append(sanitize(name))
                    .append(' ')
                    .append(sanitize(value))
                    .append(' ')
                    .append(Long.toString(timestamp))
                    .append('\n');
            byte[] dataBytes = packetData.toString().getBytes(this.charset);
            logger.trace("Sending " + packetData);
            this.socket.send(new DatagramPacket(dataBytes, dataBytes.length, this.address.getAddress(),
                    this.address.getPort()));
            this.failures = 0;
        } catch (IOException e) {
            logger.error("Error while sending packet", e);
            failures++;
            throw e;
        }
    }

    /**
     * Returns the number of failed writes to the server.
     *
     * @return the number of failed writes to the server
     */
    public int getFailures () {
        return failures;
    }

    @Override
    public void close () throws IOException {
        if (socket != null) {
            socket.close();
        }
        this.socket = null;
    }

    protected String sanitize (String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }
}
