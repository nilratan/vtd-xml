/* 
* Copyright (C) 2002-2007 XimpleWare, info@ximpleware.com
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*/
using System;
namespace com.ximpleware
{
	
	public class IndexHandler
	{
		public static void  writeIndex(byte version, int encodingType, bool ns, bool byteOrder, int nestDepth, int LCLevel, int rootIndex, byte[] xmlDoc, int docOffset, int docLen, FastLongBuffer vtdBuffer, FastLongBuffer l1Buffer, FastLongBuffer l2Buffer, FastIntBuffer l3Buffer, System.IO.Stream os)
		{
			if (xmlDoc == null || docLen <= 0 || vtdBuffer == null || vtdBuffer.size() == 0 || l1Buffer == null || l2Buffer == null || l3Buffer == null)
			{
				throw new IndexWriteException("Invalid VTD index ");
			}
			int i;
			//UPGRADE_TODO: Class 'java.io.DataOutputStream' was converted to 'System.IO.BinaryWriter' which has a different behavior. "ms-help://MS.VSCC.v80/dv_commoner/local/redirect.htm?index='!DefaultContextWindowIndex'&keyword='jlca1073_javaioDataOutputStream'"
			System.IO.BinaryWriter dos = new System.IO.BinaryWriter(os);
			// first 4 bytes
			byte[] ba = new byte[4];
			ba[0] = (byte) version; // version # is 1 
			ba[1] = (byte) encodingType;
            if (BitConverter.IsLittleEndian == false)
                ba[2] = (byte)(ns ? 0xe0 : 0xa0); // big endien
            else
                ba[2] = (byte)(ns ? 0xc0 : 0x80);
			ba[3] = (byte) nestDepth;
			dos.Write(ba);
			// second 4 bytes
			ba[0] = 0;
			ba[1] = 4;
			ba[2] = (byte) ((rootIndex & 0xff00) >> 8);
			ba[3] = (byte) (rootIndex & 0xff);
			dos.Write(ba);
			// 2 reserved 32-bit words set to zero
			ba[1] = ba[2] = ba[3] = 0;
			dos.Write(ba);
			dos.Write(ba);
            dos.Write(ba);
            dos.Write(ba);
			// write XML doc in bytes
			dos.Write((long)docLen);
            dos.Write(xmlDoc, docOffset, docLen);
			//dos.Write(xmlDoc, docOffset, docLen);
			// zero padding to make it integer multiple of 64 bits
			if ((docLen & 0x07) != 0)
			{
				int t = (((docLen >> 3) + 1) << 3) - docLen;
				for (; t > 0; t--)
					dos.Write((System.Byte) 0);
			}
			// write VTD
            
			dos.Write((long)vtdBuffer.size());
			for (i = 0; i < vtdBuffer.size(); i++)
			{
				dos.Write(vtdBuffer.longAt(i));
			}
			// write L1 
			dos.Write((long)l1Buffer.size());
			for (i = 0; i < l1Buffer.size(); i++)
			{
				dos.Write(l1Buffer.longAt(i));
			}
			// write L2
			dos.Write((long)l2Buffer.size());
			for (i = 0; i < l2Buffer.size(); i++)
			{
				dos.Write(l2Buffer.longAt(i));
			}
			// write L3
			dos.Write((long)l3Buffer.size());
			for (i = 0; i < l3Buffer.size(); i++)
			{
				dos.Write(l3Buffer.intAt(i));
			}
			// pad zero if # of l3 entry is odd
			if ((l3Buffer.size() & 1) != 0)
				dos.Write(0);
			dos.Close();
		}
		
		public static void  readIndex(System.IO.Stream is_Renamed, VTDGen vg)
		{
			if (is_Renamed == null || vg == null)
				throw new IndexReadException("Invalid argument(s) for readIndex()");
			//UPGRADE_TODO: Class 'java.io.DataInputStream' was converted to 'System.IO.BinaryReader' 
            //which has a different behavior. 
            //"ms-help://MS.VSCC.v80/dv_commoner/local/redirect.htm?index='!DefaultContextWindowIndex'&keyword='jlca1073_javaioDataInputStream'"
			System.IO.BinaryReader dis = new System.IO.BinaryReader(is_Renamed);
			byte b = dis.ReadByte(); // first byte
			// no check on version number for now
			// second byte
			vg.encoding = (sbyte) dis.ReadByte();
			int intLongSwitch;
			int ns;
			int endian;
			// third byte
			b = dis.ReadByte();
			if ((b & 0x80) != 0)
				intLongSwitch = 1;
			//use ints
			else
				intLongSwitch = 0;
			if ((b & 0x40) != 0)
				vg.ns = true;
			else
				vg.ns = false;
			if ((b & 0x20) != 0)
				endian = 1;
			else
				endian = 0;
			
			// fourth byte
			vg.VTDDepth = dis.ReadByte();
			
			// 5th and 6th byte
			int LCLevels = (((int) dis.ReadByte()) << 8) | dis.ReadByte();
			if (LCLevels < 3)
				throw new IndexReadException("LC levels must be at least 3");
			// 7th and 8th byte
			vg.rootIndex = (((int) dis.ReadByte()) << 8) | dis.ReadByte();
			
			// skip a long
			long l =dis.ReadInt64();
            Console.WriteLine(" l ==>" + l);
            l =dis.ReadInt64();
            Console.WriteLine(" l ==>" + l);
            l = dis.ReadInt64();
            Console.WriteLine(" l ==>" + l);
			int size;
			// read XML size
            if (BitConverter.IsLittleEndian && endian == 0
                || BitConverter.IsLittleEndian == false && endian == 1)
                size = (int)l;
            else
                size = (int)reverseLong(l);
            
			
			// read XML bytes
			byte[] XMLDoc = new byte[size];
			dis.Read(XMLDoc,0,size);
			if ((size & 0x7) != 0)
			{
				int t = (((size >> 3) + 1) << 3) - size;
                while (t > 0)
                {
                    dis.ReadByte();
                    t--;
                }
			}
			
			vg.setDoc(XMLDoc);

            if (BitConverter.IsLittleEndian && endian == 0
                || BitConverter.IsLittleEndian == false && endian == 1)
			{
				// read vtd records
				int vtdSize = (int) dis.ReadInt64();
				while (vtdSize > 0)
				{
					vg.VTDBuffer.append(dis.ReadInt64());
					vtdSize--;
				}
				// read L1 LC records
				int l1Size = (int) dis.ReadInt64();
				while (l1Size > 0)
				{
					vg.l1Buffer.append(dis.ReadInt64());
					l1Size--;
				}
				// read L2 LC records
				int l2Size = (int) dis.ReadInt64();
				while (l2Size > 0)
				{
					vg.l2Buffer.append(dis.ReadInt64());
					l2Size--;
				}
				// read L3 LC records
				int l3Size = (int) dis.ReadInt64();
				if (intLongSwitch == 1)
				{
					//l3 uses ints
					while (l3Size > 0)
					{
						vg.l3Buffer.append(dis.ReadInt32());
						l3Size--;
					}
				}
				else
				{
					while (l3Size > 0)
					{
						vg.l3Buffer.append((int) (dis.ReadInt64() >> 32));
						l3Size--;
					}
				}
			}
			else
			{
				// read vtd records
				int vtdSize = (int) reverseLong(dis.ReadInt64());
				while (vtdSize > 0)
				{
					vg.VTDBuffer.append(reverseLong(dis.ReadInt64()));
					vtdSize--;
				}
				// read L1 LC records
				int l1Size = (int) reverseLong(dis.ReadInt64());
				while (l1Size > 0)
				{
					vg.l1Buffer.append(reverseLong(dis.ReadInt64()));
					l1Size--;
				}
				// read L2 LC records
				int l2Size = (int) reverseLong(dis.ReadInt64());
				while (l2Size > 0)
				{
					vg.l2Buffer.append(reverseLong(dis.ReadInt64()));
					l2Size--;
				}
				// read L3 LC records
				int l3Size = (int) reverseLong(dis.ReadInt64());
				if (intLongSwitch == 1)
				{
					//l3 uses ints
					while (l3Size > 0)
					{
						vg.l3Buffer.append(reverseInt(dis.ReadInt32()));
						l3Size--;
					}
				}
				else
				{
					while (l3Size > 0)
					{
						vg.l3Buffer.append(reverseInt((int) (dis.ReadInt64() >> 32)));
						l3Size--;
					}
				}
			}
		}
		
		private static long reverseLong(long l)
		{
			//UPGRADE_TODO: Literal detected as an unsigned long can generate compilation errors. "ms-help://MS.VSCC.v80/dv_commoner/local/redirect.htm?index='!DefaultContextWindowIndex'&keyword='jlca1175'"
			long t = (((l & -0x0100000000000000L) >> 56) & 0xffL)
                | ((l & 0xff000000000000L) >> 40) 
                | ((l & 0xff0000000000L) >> 24) 
                | ((l & 0xff00000000L) >> 8) 
                | ((l & 0xff000000L) << 8) 
                | ((l & 0xff0000L) << 24) 
                | ((l & 0xff00L) << 40) 
                | ((l & 0xffL) << 56);
			return t;
		}
		
		private static int reverseInt(int i)
		{
			int t = (((i & -0x01000000) >> 24) & 0xff) 
                | ((i & 0xff0000) >> 8) 
                | ((i & 0xff00) << 8) 
                | ((i & 0xff) << 24);
			return t;
		}
	}
}