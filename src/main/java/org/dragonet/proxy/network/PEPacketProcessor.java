/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 *                       Version 3, 29 June 2007
 *
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 * You can view LICENCE file for details. 
 *
 * @author The Dragonet Team
 */
package org.dragonet.proxy.network;

import java.util.ArrayDeque;
import java.util.Deque;
import lombok.Getter;
import org.dragonet.net.packet.Protocol;
import org.dragonet.net.packet.minecraft.BatchPacket;
import org.dragonet.net.packet.minecraft.LoginPacket;
import org.dragonet.net.packet.minecraft.PEPacket;
import org.dragonet.net.packet.minecraft.PEPacketIDs;

public class PEPacketProcessor implements Runnable {

    public final static int MAX_PACKETS_PER_CYCLE = 200;
    
    @Getter
    private final UpstreamSession client;

    private final Deque<byte[]> packets = new ArrayDeque<>();
    
    public PEPacketProcessor(UpstreamSession client) {
        this.client = client;
    }
    
    public void putPacket(byte[] packet){
        packets.add(packet);
    }
    
    @Override
    public void run() {
        int cnt = 0;
        while(cnt < MAX_PACKETS_PER_CYCLE && !packets.isEmpty()){
            cnt++;
            byte[] bin = packets.pop();
            PEPacket packet = Protocol.decode(bin);
            if(packet == null) continue;
            handlePacket(packet);
        }
    }
    
    public void handlePacket(PEPacket packet){
        if(packet == null) return;
        if(BatchPacket.class.isAssignableFrom(packet.getClass())){
            for(PEPacket pk : ((BatchPacket)packet).packets){
                if(pk == null) continue;
                handlePacket(pk);
            }
            return;
        }
        client.getProxy().getLogger().info("Received packet: " + packet.getClass().getSimpleName());
        //TODO
        switch(packet.pid()){
            case PEPacketIDs.LOGIN_PACKET:
                client.onLogin((LoginPacket)packet);
                break;
            default:
                //TODO: Translate and send
                break;
        }
    }

    
}