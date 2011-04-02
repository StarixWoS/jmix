/*
    *  Licensed to the Apache Software Foundation (ASF) under one
    *  or more contributor license agreements.  See the NOTICE file
    *  distributed with this work for additional information
    *  regarding copyright ownership.  The ASF licenses this file
    *  to you under the Apache License, Version 2.0 (the
    *  "License"); you may not use this file except in compliance
    *  with the License.  You may obtain a copy of the License at
    *
   *    http://www.apache.org/licenses/LICENSE-2.0
   *
   *  Unless required by applicable law or agreed to in writing,
   *  software distributed under the License is distributed on an
   *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   *  KIND, either express or implied.  See the License for the
   *  specific language governing permissions and limitations
   *  under the License.
   *
   */
//  
//  import java.net.SocketAddress;
//  import java.util.Queue;
//  
//  import org.apache.mina.core.buffer.IoBuffer;
//  import org.apache.mina.core.file.FileRegion;
//  import org.apache.mina.core.filterchain.IoFilter;
//  import org.apache.mina.core.filterchain.IoFilterAdapter;
//  import org.apache.mina.core.filterchain.IoFilterChain;
//  import org.apache.mina.core.future.DefaultWriteFuture;
//  import org.apache.mina.core.future.WriteFuture;
//  import org.apache.mina.core.session.AttributeKey;
//  import org.apache.mina.core.session.IoSession;
//  import org.apache.mina.core.write.DefaultWriteRequest;
//  import org.apache.mina.core.write.NothingWrittenException;
//  import org.apache.mina.core.write.WriteRequest;
//  import org.apache.mina.core.write.WriteRequestWrapper;
//  import org.slf4j.Logger;
//  import org.slf4j.LoggerFactory;
//  
//  /**
//   * An {@link IoFilter} which translates binary or protocol specific data into
//   * message object and vice versa using {@link ProtocolCodecFactory},
//   * {@link ProtocolEncoder}, or {@link ProtocolDecoder}.
//   *
//   * @author The Apache MINA Project (dev@mina.apache.org)
//   * @version $Rev: 713363 $, $Date: 2008-11-12 14:31:57 +0100 (Wed, 12 Nov 2008) $
//   * @org.apache.xbean.XBean
//   */
//  public class ProtocolCodecFilter extends IoFilterAdapter {
//  
//      private static final Class<?>[] EMPTY_PARAMS = new Class[0];
//      private static final IoBuffer EMPTY_BUFFER = IoBuffer.wrap(new byte[0]);
//  
//      private final AttributeKey ENCODER = new AttributeKey(getClass(), "encoder");
//      private final AttributeKey DECODER = new AttributeKey(getClass(), "decoder");
//      private final AttributeKey DECODER_OUT = new AttributeKey(getClass(), "decoderOut");
//      private final AttributeKey ENCODER_OUT = new AttributeKey(getClass(), "encoderOut");
//      
//      /** The factory responsible for creating the encoder and decoder */
//      private final ProtocolCodecFactory factory;
//  
//      private final Logger logger = LoggerFactory.getLogger(getClass());
//  
//      /**
//       * 
//       * Creates a new instance of ProtocolCodecFilter, associating a factory
//       * for the creation of the encoder and decoder.
//       *
//       * @param factory The associated factory
//       */
//      public ProtocolCodecFilter(ProtocolCodecFactory factory) {
//          if (factory == null) {
//              throw new NullPointerException("factory");
//          }
//          this.factory = factory;
//      }
//  
//      
//      /**
//       * Creates a new instance of ProtocolCodecFilter, without any factory.
//       * The encoder/decoder factory will be created as an inner class, using
//       * the two parameters (encoder and decoder). 
//       * 
//       * @param encoder The class responsible for encoding the message
//       * @param decoder The class responsible for decoding the message
//       */
//      public ProtocolCodecFilter(final ProtocolEncoder encoder,
//              final ProtocolDecoder decoder) {
//          if (encoder == null) {
//              throw new NullPointerException("encoder");
//          }
//          if (decoder == null) {
//              throw new NullPointerException("decoder");
//          }
//  
//          // Create the inner Factory based on the two parameters
//          this.factory = new ProtocolCodecFactory() {
//              public ProtocolEncoder getEncoder(IoSession session) {
//                 return encoder;
//             }
// 
//             public ProtocolDecoder getDecoder(IoSession session) {
//                 return decoder;
//             }
//         };
//     }
// 
//     /**
//      * Creates a new instance of ProtocolCodecFilter, without any factory.
//      * The encoder/decoder factory will be created as an inner class, using
//      * the two parameters (encoder and decoder), which are class names. Instances
//      * for those classes will be created in this constructor.
//      * 
//      * @param encoder The class responsible for encoding the message
//      * @param decoder The class responsible for decoding the message
//      */
//     public ProtocolCodecFilter(
//             final Class<? extends ProtocolEncoder> encoderClass,
//             final Class<? extends ProtocolDecoder> decoderClass) {
//         if (encoderClass == null) {
//             throw new NullPointerException("encoderClass");
//         }
//         if (decoderClass == null) {
//             throw new NullPointerException("decoderClass");
//         }
//         if (!ProtocolEncoder.class.isAssignableFrom(encoderClass)) {
//             throw new IllegalArgumentException("encoderClass: "
//                     + encoderClass.getName());
//         }
//         if (!ProtocolDecoder.class.isAssignableFrom(decoderClass)) {
//             throw new IllegalArgumentException("decoderClass: "
//                     + decoderClass.getName());
//         }
//         try {
//             encoderClass.getConstructor(EMPTY_PARAMS);
//         } catch (NoSuchMethodException e) {
//             throw new IllegalArgumentException(
//                     "encoderClass doesn't have a public default constructor.");
//         }
//         try {
//             decoderClass.getConstructor(EMPTY_PARAMS);
//         } catch (NoSuchMethodException e) {
//             throw new IllegalArgumentException(
//                     "decoderClass doesn't have a public default constructor.");
//         }
// 
//         // Create the inner Factory based on the two parameters. We instanciate
//         // the encoder and decoder locally.
//         this.factory = new ProtocolCodecFactory() {
//             public ProtocolEncoder getEncoder(IoSession session) throws Exception {
//                 return encoderClass.newInstance();
//             }
// 
//             public ProtocolDecoder getDecoder(IoSession session) throws Exception {
//                 return decoderClass.newInstance();
//             }
//         };
//     }
// 
//     
//     /**
//      * Get the encoder instance from a given session.
//      *
//      * @param session The associated session we will get the encoder from
//      * @return The encoder instance, if any
//      */
//     public ProtocolEncoder getEncoder(IoSession session) {
//         return (ProtocolEncoder) session.getAttribute(ENCODER);
//     }
// 
//     @Override
//     public void onPreAdd(IoFilterChain parent, String name,
//             NextFilter nextFilter) throws Exception {
//         if (parent.contains(this)) {
//             throw new IllegalArgumentException(
//                     "You can't add the same filter instance more than once.  Create another instance and add it.");
//         }
//         
//         // Initialize the encoder and decoder
//         initCodec(parent.getSession());
//     }
// 
//     @Override
//     public void onPostRemove(IoFilterChain parent, String name,
//             NextFilter nextFilter) throws Exception {
//         // Clean everything
//         disposeCodec(parent.getSession());
//     }
// 
//     /**
//      * Process the incoming message, calling the session decoder. As the incoming
//      * buffer might contains more than one messages, we have to loop until the decoder
//      * throws an exception.
//      * 
//      *  while ( buffer not empty )
//      *    try 
//      *      decode ( buffer )
//      *    catch
//      *      break;
//      *    
//      */
//     @Override
//     public void messageReceived(NextFilter nextFilter, IoSession session,
//             Object message) throws Exception {
//         if (!(message instanceof IoBuffer)) {
//             nextFilter.messageReceived(session, message);
//             return;
//         }
// 
//         IoBuffer in = (IoBuffer) message;
//         ProtocolDecoder decoder = getDecoder(session);
//         ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);
//         
//         // Loop until we don't have anymore byte in the buffer,
//         // or until the decoder throws an unrecoverable exception or 
//         // can't decoder a message, because there are not enough 
//         // data in the buffer
//         while (in.hasRemaining()) {
//             int oldPos = in.position();
//             try {
//                 synchronized (decoderOut) {
//                     // Call the decoder with the read bytes
//                     decoder.decode(session, in, decoderOut);
//                 }
//                 
//                 // Finish decoding if no exception was thrown.
//                 decoderOut.flush(nextFilter, session);
//             } catch (Throwable t) {
//                 ProtocolDecoderException pde;
//                 if (t instanceof ProtocolDecoderException) {
//                     pde = (ProtocolDecoderException) t;
//                 } else {
//                     pde = new ProtocolDecoderException(t);
//                 }
//                 
//                 if (pde.getHexdump() == null) {
//                     // Generate a message hex dump
//                     int curPos = in.position();
//                     in.position(oldPos);
//                     pde.setHexdump(in.getHexDump());
//                     in.position(curPos);
//                 }
// 
//                 // Fire the exceptionCaught event.
//                 decoderOut.flush(nextFilter, session);
//                 nextFilter.exceptionCaught(session, pde);
// 
//                 // Retry only if the type of the caught exception is
//                 // recoverable and the buffer position has changed.
//                 // We check buffer position additionally to prevent an
//                 // infinite loop.
//                 if (!(t instanceof RecoverableProtocolDecoderException) ||
//                         (in.position() == oldPos)) {
//                     break;
//                 }
//             }
//         }
//     }
// 
//     @Override
//     public void messageSent(NextFilter nextFilter, IoSession session,
//             WriteRequest writeRequest) throws Exception {
//         if (writeRequest instanceof EncodedWriteRequest) {
//             return;
//         }
// 
//         if (!(writeRequest instanceof MessageWriteRequest)) {
//             nextFilter.messageSent(session, writeRequest);
//             return;
//         }
// 
//         MessageWriteRequest wrappedRequest = (MessageWriteRequest) writeRequest;
//         nextFilter.messageSent(session, wrappedRequest.getParentRequest());
//     }
// 
//     @Override
//     public void filterWrite(NextFilter nextFilter, IoSession session,
//             WriteRequest writeRequest) throws Exception {
//         Object message = writeRequest.getMessage();
//         
//         // Bypass the encoding if the message is contained in a ByteBuffer,
//         // as it has already been encoded before
//         if (message instanceof IoBuffer || message instanceof FileRegion) {
//             nextFilter.filterWrite(session, writeRequest);
//             return;
//         }
// 
//         // Get the encoder in the session
//         ProtocolEncoder encoder = getEncoder(session);
// 
//         ProtocolEncoderOutput encoderOut = getEncoderOut(session,
//                 nextFilter, writeRequest);
// 
//         try {
//             // Now we can try to encode the response
//             encoder.encode(session, message, encoderOut);
//             
//             // Send it directly
//             ((ProtocolEncoderOutputImpl)encoderOut).flushWithoutFuture();
//             
//             // Call the next filter
//             nextFilter.filterWrite(session, new MessageWriteRequest(
//                     writeRequest));
//         } catch (Throwable t) {
//             ProtocolEncoderException pee;
//             
//             // Generate the correct exception
//             if (t instanceof ProtocolEncoderException) {
//                 pee = (ProtocolEncoderException) t;
//             } else {
//                 pee = new ProtocolEncoderException(t);
//             }
//             
//             throw pee;
//         }
//     }
//     
//     /**
//      * Associate a decoder and encoder instances to the newly created session.
//      * <br>
//      * <br>
//      * In order to get the encoder and decoder crea
//      * 
//      * @param nextFilter The next filter to invoke when having processed the current 
//      * method
//      * @param session The newly created session
//      * @throws Exception if we can't create instances of the decoder or encoder
//      */
//     @Override
//     public void sessionCreated(NextFilter nextFilter, IoSession session) throws Exception {
//         // Initialized the encoder and decoder
//         initCodec(session);
// 
//         // Call the next filter
//         nextFilter.sessionCreated(session);
//     }
// 
//     @Override
//     public void sessionClosed(NextFilter nextFilter, IoSession session)
//             throws Exception {
//         // Call finishDecode() first when a connection is closed.
//         ProtocolDecoder decoder = getDecoder(session);
//         ProtocolDecoderOutput decoderOut = getDecoderOut(session, nextFilter);
//         
//         try {
//             decoder.finishDecode(session, decoderOut);
//         } catch (Throwable t) {
//             ProtocolDecoderException pde;
//             if (t instanceof ProtocolDecoderException) {
//                 pde = (ProtocolDecoderException) t;
//             } else {
//                 pde = new ProtocolDecoderException(t);
//             }
//             throw pde;
//         } finally {
//             // Dispose everything
//             disposeCodec(session);
//             decoderOut.flush(nextFilter, session);
//         }
// 
//         // Call the next filter
//         nextFilter.sessionClosed(session);
//     }
// 
//     private static class EncodedWriteRequest extends DefaultWriteRequest {
//         private EncodedWriteRequest(Object encodedMessage,
//                 WriteFuture future, SocketAddress destination) {
//             super(encodedMessage, future, destination);
//         }
//     }
// 
//     private static class MessageWriteRequest extends WriteRequestWrapper {
//         private MessageWriteRequest(WriteRequest writeRequest) {
//             super(writeRequest);
//         }
// 
//         @Override
//         public Object getMessage() {
//             return EMPTY_BUFFER;
//         }
//     }
// 
//     private static class ProtocolDecoderOutputImpl extends
//             AbstractProtocolDecoderOutput {
//         public ProtocolDecoderOutputImpl() {
//         }
// 
//         public void flush(NextFilter nextFilter, IoSession session) {
//             Queue<Object> messageQueue = getMessageQueue();
//             while (!messageQueue.isEmpty()) {
//                 nextFilter.messageReceived(session, messageQueue.poll());
//             }
//         }
//     }
// 
//     private static class ProtocolEncoderOutputImpl extends
//             AbstractProtocolEncoderOutput {
//         private final IoSession session;
// 
//         private final NextFilter nextFilter;
// 
//         private final WriteRequest writeRequest;
// 
//         public ProtocolEncoderOutputImpl(IoSession session,
//                 NextFilter nextFilter, WriteRequest writeRequest) {
//             this.session = session;
//             this.nextFilter = nextFilter;
//             this.writeRequest = writeRequest;
//         }
// 
//         public WriteFuture flush() {
//             Queue<Object> bufferQueue = getMessageQueue();
//             WriteFuture future = null;
//             for (;;) {
//                 Object encodedMessage = bufferQueue.poll();
//                 if (encodedMessage == null) {
//                     break;
//                 }
// 
//                 // Flush only when the buffer has remaining.
//                 if (!(encodedMessage instanceof IoBuffer) ||
//                         ((IoBuffer) encodedMessage).hasRemaining()) {
//                     future = new DefaultWriteFuture(session);
//                     nextFilter.filterWrite(session, new EncodedWriteRequest(encodedMessage,
//                             future, writeRequest.getDestination()));
//                 }
//             }
// 
//             if (future == null) {
//                 future = DefaultWriteFuture.newNotWrittenFuture(
//                         session, new NothingWrittenException(writeRequest));
//             }
// 
//             return future;
//         }
//         
//         public void flushWithoutFuture() {
//             Queue<Object> bufferQueue = getMessageQueue();
//             for (;;) {
//                 Object encodedMessage = bufferQueue.poll();
//                 if (encodedMessage == null) {
//                     break;
//                 }
// 
//                 // Flush only when the buffer has remaining.
//                 if (!(encodedMessage instanceof IoBuffer) ||
//                         ((IoBuffer) encodedMessage).hasRemaining()) {
//                     SocketAddress destination = writeRequest.getDestination();
//                     WriteRequest writeRequest = new EncodedWriteRequest(
//                         encodedMessage, null, destination); 
//                     nextFilter.filterWrite(session, writeRequest);
//                 }
//             }
//         }
//     }
//     
//     //----------- Helper methods ---------------------------------------------
//     /**
//      * Initialize the encoder and the decoder, storing them in the 
//      * session attributes.
//      */
//     private void initCodec(IoSession session) throws Exception {
//         // Creates the decoder and stores it into the newly created session 
//         ProtocolDecoder decoder = factory.getDecoder(session);
//         session.setAttribute(DECODER, decoder);
// 
//         // Creates the encoder and stores it into the newly created session 
//         ProtocolEncoder encoder = factory.getEncoder(session);
//         session.setAttribute(ENCODER, encoder);
//     }
//     
//     /**
//      * Dispose the encoder, decoder, and the callback for the decoded
//      * messages.
//      */
//     private void disposeCodec(IoSession session) {
//         // We just remove the two instances of encoder/decoder to release resources
//         // from the session
//         disposeEncoder(session);
//         disposeDecoder(session);
//         
//         // We also remove the callback  
//         disposeDecoderOut(session);
//     }
//     
//     /**
//      * dispose the encoder, removing its instance from the
//      * session's attributes, and calling the associated
//      * dispose method.
//      */
//     private void disposeEncoder(IoSession session) {
//         ProtocolEncoder encoder = (ProtocolEncoder) session
//                 .removeAttribute(ENCODER);
//         if (encoder == null) {
//             return;
//         }
// 
//         try {
//             encoder.dispose(session);
//         } catch (Throwable t) {
//             logger.warn(
//                     "Failed to dispose: " + encoder.getClass().getName() + " (" + encoder + ')');
//         }
//     }
// 
//     /**
//      * Get the decoder instance from a given session.
//      *
//      * @param session The associated session we will get the decoder from
//      * @return The decoder instance
//      */
//     private ProtocolDecoder getDecoder(IoSession session) {
//         return (ProtocolDecoder) session.getAttribute(DECODER);
//     }
// 
//     /**
//      * dispose the decoder, removing its instance from the
//      * session's attributes, and calling the associated
//      * dispose method.
//      */
//     private void disposeDecoder(IoSession session) {
//         ProtocolDecoder decoder = (ProtocolDecoder) session
//                 .removeAttribute(DECODER);
//         if (decoder == null) {
//             return;
//         }
// 
//         try {
//             decoder.dispose(session);
//         } catch (Throwable t) {
//             logger.warn(
//                     "Falied to dispose: " + decoder.getClass().getName() + " (" + decoder + ')');
//         }
//     }
// 
//     /**
//      * Return a reference to the decoder callback. If it's not already created
//      * and stored into the session, we create a new instance.
//      */
//     private ProtocolDecoderOutput getDecoderOut(IoSession session,
//             NextFilter nextFilter) {
//         ProtocolDecoderOutput out = (ProtocolDecoderOutput) session.getAttribute(DECODER_OUT);
//         
//         if (out == null) {
//             // Create a new instance, and stores it into the session
//             out = new ProtocolDecoderOutputImpl();
//             session.setAttribute(DECODER_OUT, out);
//         }
//         
//         return out;
//     }
// 
//     private ProtocolEncoderOutput getEncoderOut(IoSession session,
//         NextFilter nextFilter, WriteRequest writeRequest) {
//         ProtocolEncoderOutput out = (ProtocolEncoderOutput) session.getAttribute(ENCODER_OUT);
//         
//         if (out == null) {
//             // Create a new instance, and stores it into the session
//             out = new ProtocolEncoderOutputImpl(session, nextFilter, writeRequest);
//             session.setAttribute(ENCODER_OUT, out);
//         }
//         
//         return out;
//     }
// 
//     /**
//      * Remove the decoder callback from the session's attributes.
//      */
//     private void disposeDecoderOut(IoSession session) {
//         session.removeAttribute(DECODER_OUT);
//     }
// }
