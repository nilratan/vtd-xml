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
package com.ximpleware;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;
import com.ximpleware.xpath.Alist;
import com.ximpleware.xpath.Expr;
import com.ximpleware.xpath.FuncName;
import com.ximpleware.xpath.UnsupportedException;
import com.ximpleware.xpath.XPathEvalException;

/**
 * FuncExpr implements the function expression defined in XPath spec
 * 
 */
public class FuncExpr extends Expr {
    private static Logger logger = Logger.getLogger(FuncExpr.class);
    
    public Alist argumentList;

    public int opCode;

    boolean isNumerical;

    boolean isBoolean;

    boolean isString;

    int contextSize;

    // double d;
    int position;

    int a;

    int argCount() {
        Alist temp = argumentList;
        int count = 0;
        while (temp != null) {
            count++;
            temp = temp.next;
        }
        return count;
    }

    public FuncExpr(int oc, Alist list) {
        a = 0;
        opCode = oc;
        argumentList = list;
        isBoolean = false;
        isString = false;
        position = 0;
        // isNodeSet = false;
        isNumerical = false;
        switch (opCode) {
        case FuncName.LAST:
            isNumerical = true;
            break;
        case FuncName.POSITION:
            isNumerical = true;
            break;
        case FuncName.COUNT:
            isNumerical = true;
            break;
        case FuncName.LOCAL_NAME:
            isString = true;
            break;
        case FuncName.NAMESPACE_URI:
            isString = true;
            break;
        case FuncName.NAME:
            isString = true;
            break;
        case FuncName.STRING:
            isString = true;
            break;
        case FuncName.CONCAT:
            isString = true;
            break;
        case FuncName.STARTS_WITH:
            isBoolean = true;
            break;
        case FuncName.CONTAINS:
            isBoolean = true;
            break;
        case FuncName.SUBSTRING_BEFORE:
            isString = true;
            break;
        case FuncName.SUBSTRING_AFTER:
            isString = true;
            break;
        case FuncName.SUBSTRING:
            isString = true;
            break;
        case FuncName.STRING_LENGTH:
            isNumerical = true;
            break;
        case FuncName.NORMALIZE_SPACE:
            isString = true;
            break;
        case FuncName.TRANSLATE:
            isString = true;
            break;
        case FuncName.BOOLEAN:
            isBoolean = true;
            break;
        case FuncName.NOT:
            isBoolean = true;
            break;
        case FuncName.TRUE:
            isBoolean = true;
            break;
        case FuncName.FALSE:
            isBoolean = true;
            break;
        case FuncName.LANG:
            isBoolean = true;
            break;
        case FuncName.NUMBER:
            isNumerical = true;
            break;
        case FuncName.SUM:
            isNumerical = true;
            break;
        case FuncName.FLOOR:
            isNumerical = true;
            break;
        case FuncName.CEILING:
            isNumerical = true;
            break;
        // added by yanj at 2007-07-08
        case FuncName.GP_GEOMETRY_TYPE:
            isString = true;
            break;
        case FuncName.GP_WITHIN:
            isBoolean = true;
            break;
        case FuncName.GP_INTERSECTION:  //! FuncExpr cannt support nodeset now
            isString = true;
            break;
        // added end
        default:
            isNumerical = true;
            break;
        }
    }

    public String toString() {
        if (argumentList == null)
            return fname() + " (" + ")";
        return fname() + " (" + argumentList + ")";
    }

    private String getLocalName(VTDNav vn) {
        if (argCount() == 0) {
            try {
                int index = vn.getCurrentIndex();
                int type = vn.getTokenType(index);
                if (vn.ns
                        && (type == VTDNav.TOKEN_STARTING_TAG || type == VTDNav.TOKEN_ATTR_NAME)) {
                    int offset = vn.getTokenOffset(index);
                    int length = vn.getTokenLength(index);
                    if (length < 0x10000)
                        return vn.toRawString(index);
                    else {
                        int preLen = length >> 16;
                        int QLen = length & 0xffff;
                        if (preLen != 0)
                            return vn.toRawString(offset + preLen + 1, QLen
                                    - preLen - 1);
                        else {
                            return vn.toRawString(offset, QLen);
                        }
                    }
                } else
                    return "";
            } catch (NavException e) {
                return ""; // this will almost never occur
            }

        } else if (argCount() == 1) {
            int a = -1;
            vn.push2();
            try {
                a = argumentList.e.evalNodeSet(vn);
                argumentList.e.reset(vn);
                vn.pop2();
            } catch (Exception e) {
                argumentList.e.reset(vn);
                vn.pop2();
            }

            if (a == -1 || vn.ns == false)
                return "";
            int type = vn.getTokenType(a);
            if (type != VTDNav.TOKEN_STARTING_TAG
                    && type != VTDNav.TOKEN_ATTR_NAME)
                return "";
            try {
                int offset = vn.getTokenOffset(a);
                int length = vn.getTokenLength(a);
                if (length < 0x10000)
                    return vn.toRawString(a);
                else {
                    int preLen = length >> 16;
                    int QLen = length & 0xffff;
                    if (preLen != 0)
                        return vn.toRawString(offset + preLen + 1, QLen
                                - preLen - 1);
                    else {
                        return vn.toRawString(offset, QLen);
                    }
                }
            } catch (NavException e) {
                return ""; // this will almost never occur
            }
        } else
            throw new IllegalArgumentException(
                    "local-name()'s argument count is invalid");
    }

    private String getNameSpaceURI(VTDNav vn) {
        if (argCount() == 0) {
            try {
                int i = vn.getCurrentIndex();
                int type = vn.getTokenType(i);

                if (vn.ns
                        && (type == VTDNav.TOKEN_STARTING_TAG || type == VTDNav.TOKEN_ATTR_NAME)) {
                    int a = vn.lookupNS();
                    if (a == 0)
                        return "";
                    else
                        return vn.toString(a);
                }
                return "";
            } catch (Exception e) {
                return "";
            }
        } else if (argCount() == 1) {
            int a = -1;
            vn.push2();
            try {
                a = argumentList.e.evalNodeSet(vn);
                argumentList.e.reset(vn);
                vn.pop2();
            } catch (Exception e) {
                argumentList.e.reset(vn);
                vn.pop2();
            }
            try {
                if (a == -1 || vn.ns == false)
                    return "";
                else {
                    int type = vn.getTokenType(a);
                    if (type == VTDNav.TOKEN_STARTING_TAG
                            || type == VTDNav.TOKEN_ATTR_NAME)
                        return vn.toString(vn.lookupNS());
                    return "";
                }
            } catch (Exception e) {
            }
            ;
            return "";

        } else
            throw new IllegalArgumentException(
                    "namespace-uri()'s argument count is invalid");
    }

    private String getName(VTDNav vn) {
        int a;
        if (argCount() == 0) {
            a = vn.getCurrentIndex();
            int type = vn.getTokenType(a);

            if (type == VTDNav.TOKEN_STARTING_TAG
                    || type == VTDNav.TOKEN_ATTR_NAME) {
                try {
                    return vn.toString(a);
                } catch (Exception e) {
                    return "";
                }
            } else
                return "";
        } else if (argCount() == 1) {
            a = -1;
            vn.push2();
            try {
                a = argumentList.e.evalNodeSet(vn);
                argumentList.e.reset(vn);
                vn.pop2();
            } catch (Exception e) {
                argumentList.e.reset(vn);
                vn.pop2();
            }
            try {
                if (a == -1 || vn.ns == false)
                    return "";
                else {
                    int type = vn.getTokenType(a);
                    if (type == VTDNav.TOKEN_STARTING_TAG
                            || type == VTDNav.TOKEN_ATTR_NAME)
                        return vn.toString(a);
                    return "";
                }
            } catch (Exception e) {
            }
            return "";
        } else
            throw new IllegalArgumentException(
                    "name()'s argument count is invalid");

    }

    /*
     * private boolean lang(VTDNav vn){ return false; }
     */
    private boolean startsWith(VTDNav vn) {
        String s1 = argumentList.e.evalString(vn);
        String s2 = argumentList.next.e.evalString(vn);
        return s1.startsWith(s2);
    }

    private boolean contains(VTDNav vn) {
        String s1 = argumentList.e.evalString(vn);
        String s2 = argumentList.next.e.evalString(vn);
        // return s1.contains(s2);
        return s1.indexOf(s2) != -1;
        // return (s1.i))
    }

    private String subString(VTDNav vn) {
        if (argCount() == 2) {
            String s = argumentList.e.evalString(vn);
            double d1 = Math.floor(argumentList.next.e.evalNumber(vn) + 0.5d);
            if (d1 != d1 || d1 > s.length())
                return "";
            return s.substring(Math.max((int) (d1 - 1), 0));
        } else if (argCount() == 3) {
            String s = argumentList.e.evalString(vn);
            double d1 = Math.floor(argumentList.next.e.evalNumber(vn) + 0.5d);
            double d2 = Math
                    .floor(argumentList.next.next.e.evalNumber(vn) + 0.5d);
            // int i1 = Math.max(0, (int) d1 - 1);
            if ((d1 + d2) != (d1 + d2) || d1 > s.length())
                return "";
            return s.substring(Math.max(0, (int) d1 - 1), Math.min(s.length(),
                    (int) (d1 - 1) + (int) d2));
            // (int) argumentList.next.next.e.evalNumber(vn)-1);

        }
        throw new IllegalArgumentException(
                "substring()'s argument count is invalid");
    }

    private String subStringBefore(VTDNav vn) {
        if (argCount() == 2) {
            String s1 = argumentList.e.evalString(vn);
            String s2 = argumentList.next.e.evalString(vn);
            int len1 = s1.length();
            int len2 = s2.length();
            for (int i = 0; i < len1; i++) {
                if (s1.regionMatches(i, s2, 0, len2))
                    return s1.substring(0, i);
            }
            return "";
        }
        throw new IllegalArgumentException(
                "substring()'s argument count is invalid");
    }

    private String subStringAfter(VTDNav vn) {
        if (argCount() == 2) {
            String s1 = argumentList.e.evalString(vn);
            String s2 = argumentList.next.e.evalString(vn);
            int len1 = s1.length();
            int len2 = s2.length();
            for (int i = 0; i < len1; i++) {
                if (s1.regionMatches(i, s2, 0, len2))
                    return s1.substring(i + len2);
            }
            return "";
        }
        throw new IllegalArgumentException(
                "substring()'s argument count is invalid");
    }

    /*
     * private String translate(VTDNav vn){ return ""; }
     */
    private String normalizeSpace(VTDNav vn) {
        if (argCount() == 0) {
            String s = null;
            try {
                if (vn.atTerminal) {
                    int ttype = vn.getTokenType(vn.LN);
                    if (ttype == VTDNav.TOKEN_CDATA_VAL)
                        s = vn.toRawString(vn.LN);
                    else if (ttype == VTDNav.TOKEN_ATTR_NAME
                            || ttype == VTDNav.TOKEN_ATTR_NS) {
                        s = vn.toString(vn.LN + 1);
                    } else
                        s = vn.toString(vn.LN);
                } else {
                    s = vn.toString(vn.getCurrentIndex());
                }
                return normalize(s);
            } catch (NavException e) {
                return ""; // this will almost never occur
            }
        } else if (argCount() == 1) {
            String s = argumentList.e.evalString(vn);
            return normalize(s);
        }
        throw new IllegalArgumentException(
                "normalize-space()'s argument count is invalid");
        // return null;
    }

    private String normalize(String s) {
        int len = s.length();
        StringBuffer sb = new StringBuffer(len);
        int i = 0;
        // strip off leading ws
        for (i = 0; i < len; i++) {
            if (isWS(s.charAt(i))) {
            } else {
                break;
            }
        }
        while (i < len) {
            char c = s.charAt(i);
            if (!isWS(c)) {
                sb.append(c);
                i++;
            } else {
                while (i < len) {
                    c = s.charAt(i);
                    if (isWS(c))
                        i++;
                    else
                        break;
                }
                if (i < len)
                    sb.append(' ');
            }
        }
        return sb.toString();
    }

    private boolean isWS(char c) {
        if (c == ' ' || c == '\t' || c == '\r' || c == '\n')
            return true;
        return false;
    }

    private String concat(VTDNav vn) {
        StringBuffer sb = new StringBuffer();
        if (argCount() >= 2) {
            Alist temp = argumentList;
            while (temp != null) {
                sb.append(temp.e.evalString(vn));
                temp = temp.next;
            }
            return sb.toString();
        } else
            throw new IllegalArgumentException(
                    "concat()'s argument count is invalid");
    }

    private String getString(VTDNav vn) {
        if (argCount() == 0)
            try {
                if (vn.atTerminal) {
                    if (vn.getTokenType(vn.LN) == VTDNav.TOKEN_CDATA_VAL)
                        return vn.toRawString(vn.LN);
                    return vn.toString(vn.LN);
                }
                return vn.toString(vn.getCurrentIndex());
            } catch (NavException e) {
                return ""; // this will almost never occur
            }
        else if (argCount() == 1) {
            return argumentList.e.evalString(vn);
        } else
            throw new IllegalArgumentException(
                    "String()'s argument count is invalid");
    }

    public String evalString(VTDNav vn) throws UnsupportedException {
        switch (opCode) {
        case FuncName.CONCAT:
            return concat(vn);
            // throw new UnsupportedException("Some functions are not
            // supported");

        case FuncName.LOCAL_NAME:
            return getLocalName(vn);

        case FuncName.NAMESPACE_URI:
            return getNameSpaceURI(vn);

        case FuncName.NAME:
            return getName(vn);

        case FuncName.STRING:
            return getString(vn);

        case FuncName.SUBSTRING_BEFORE:
            return subStringBefore(vn);
        case FuncName.SUBSTRING_AFTER:
            return subStringAfter(vn);
        case FuncName.SUBSTRING:
            return subString(vn);
        case FuncName.TRANSLATE:
            throw new UnsupportedException("Some functions are not supported");
        case FuncName.NORMALIZE_SPACE:
            return normalizeSpace(vn);
            
        //added by yanj at 2007-07-26
        case FuncName.GP_GEOMETRY_TYPE:
            if (argCount() > 1)
                throw new IllegalArgumentException("gp:geometry-type() take none or one argument");
            return gpGeometryType(vn);
        case FuncName.GP_INTERSECTION:
            if ((argCount() < 1) || (argCount() > 2))
                throw new IllegalArgumentException("gp:intersection() take one or two argument");
            return gpIntersection(vn);
        //added end
            
        default:
            if (isBoolean()) {
                if (evalBoolean(vn) == true)
                    return "true";
                else
                    return "false";
            } else {
                return "" + evalNumber(vn);
            }
        }
    }

    public double evalNumber(VTDNav vn) {
        int ac = 0;
        switch (opCode) {
        case FuncName.LAST:
            if (argCount() != 0)
                throw new IllegalArgumentException(
                        "floor()'s argument count is invalid");
            return contextSize;
        case FuncName.POSITION:
            if (argCount() != 0)
                throw new IllegalArgumentException(
                        "position()'s argument count is invalid");
            return position;
        case FuncName.COUNT:
            return count(vn);
        case FuncName.NUMBER:
            if (argCount() != 1)
                throw new IllegalArgumentException(
                        "number()'s argument count is invalid");
            return argumentList.e.evalNumber(vn);

        case FuncName.SUM:
            return sum(vn);
        case FuncName.FLOOR:
            if (argCount() != 1)
                throw new IllegalArgumentException(
                        "floor()'s argument count is invalid");
            return Math.floor(argumentList.e.evalNumber(vn));

        case FuncName.CEILING:
            if (argCount() != 1)
                throw new IllegalArgumentException(
                        "ceiling()'s argument count is invalid");
            return Math.ceil(argumentList.e.evalNumber(vn));

        case FuncName.STRING_LENGTH:
            ac = argCount();
            if (ac == 0) {
                try {
                    if (vn.atTerminal == true) {
                        int type = vn.getTokenType(vn.LN);
                        if (type == VTDNav.TOKEN_ATTR_NAME
                                || type == VTDNav.TOKEN_ATTR_NS) {
                            return vn.toString(vn.LN + 1).length();
                        } else {
                            return vn.toString(vn.LN).length();
                        }
                    } else {
                        int i = vn.getText();
                        if (i == -1)
                            return 0;
                        else
                            return vn.toString(i).length();
                    }
                } catch (NavException e) {
                    return 0;
                }
            } else if (ac == 1) {
                return argumentList.e.evalString(vn).length();
            } else {
                throw new IllegalArgumentException(
                        "string-length()'s argument count is invalid");
            }

        case FuncName.ROUND:
            if (argCount() != 1)
                throw new IllegalArgumentException(
                        "round()'s argument count is invalid");
            return Math.floor(argumentList.e.evalNumber(vn)) + 0.5d;

        default:
            if (isBoolean) {
                if (evalBoolean(vn))
                    return 1;
                else
                    return 0;
            } else {
                return Double.parseDouble(evalString(vn));
            }
        }
    }

    public int evalNodeSet(VTDNav vn) throws XPathEvalException {
        throw new XPathEvalException(" Function Expr can't eval to node set ");
    }

    public boolean evalBoolean(VTDNav vn) {
        switch (opCode) {
        case FuncName.STARTS_WITH:
            if (argCount() != 2) {
                throw new IllegalArgumentException(
                        "starts-with()'s argument count is invalid");
            }
            return startsWith(vn);
        case FuncName.CONTAINS:
            if (argCount() != 2) {
                throw new IllegalArgumentException(
                        "contains()'s argument count is invalid");
            }
            return contains(vn);
        case FuncName.TRUE:
            if (argCount() != 0) {
                throw new IllegalArgumentException(
                        "true() doesn't take any argument");
            }
            return true;
        case FuncName.FALSE:
            if (argCount() != 0) {
                throw new IllegalArgumentException(
                        "false() doesn't take any argument");
            }
            return false;
        case FuncName.BOOLEAN:
            if (argCount() != 1) {
                throw new IllegalArgumentException(
                        "boolean() doesn't take any argument");
            }
            return argumentList.e.evalBoolean(vn);
        case FuncName.NOT:
            if (argCount() != 1) {
                throw new IllegalArgumentException(
                        "not() doesn't take any argument");
            }
            return !argumentList.e.evalBoolean(vn);
            
            // added by yanj at 2007-07-08
        case FuncName.GP_WITHIN:
            if ((argCount() < 1) || (argCount() > 2)){
                throw new IllegalArgumentException("gp:within() take one or two argument");
            }
            return gpWithin(vn);
            // added end
            
        default:
            if (isNumerical()) {
                double d = evalNumber(vn);
                if (d == 0 || d != d)
                    return false;
                return true;
            } else {
                return evalString(vn).length() != 0;
            }
        }
    }

    public void reset(VTDNav vn) {
        a = 0;
        // contextSize = 0;
        if (argumentList != null)
            argumentList.reset(vn);
    }

    public String fname() {
        switch (opCode) {
        case FuncName.LAST:
            return "last";
        case FuncName.POSITION:
            return "position";
        case FuncName.COUNT:
            return "count";
        case FuncName.LOCAL_NAME:
            return "local-name";
        case FuncName.NAMESPACE_URI:
            return "namespace-uri";
        case FuncName.NAME:
            return "name";
        case FuncName.STRING:
            return "string";
        case FuncName.CONCAT:
            return "concat";
        case FuncName.STARTS_WITH:
            return "starts-with";
        case FuncName.CONTAINS:
            return "contains";
        case FuncName.SUBSTRING_BEFORE:
            return "substring_before";
        case FuncName.SUBSTRING_AFTER:
            return "substring_after";
        case FuncName.SUBSTRING:
            return "substring";
        case FuncName.STRING_LENGTH:
            return "string-length";
        case FuncName.NORMALIZE_SPACE:
            return "normalize-space";
        case FuncName.TRANSLATE:
            return "translate";
        case FuncName.BOOLEAN:
            return "boolean";
        case FuncName.NOT:
            return "not";
        case FuncName.TRUE:
            return "true";
        case FuncName.FALSE:
            return "false";
        case FuncName.LANG:
            return "lang";
        case FuncName.NUMBER:
            return "number";
        case FuncName.SUM:
            return "sum";
        case FuncName.FLOOR:
            return "floor";
        case FuncName.CEILING:
            return "ceiling";
            // added by yanj at 2007-07-08
        case FuncName.GP_GEOMETRY_TYPE:
            return "gp:geometry-type";
        case FuncName.GP_WITHIN:
            return "gp:within";
        case FuncName.GP_INTERSECTION:
            return "gp:intersection";
            // added end
        default:
            return "round";
        }
    }

    public boolean isNodeSet() {
        return false;
    }

    public boolean isNumerical() {
        return isNumerical;
    }

    public boolean isString() {
        return isString;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    private int count(VTDNav vn) {
        int a = -1;
        if (argCount() != 1 || argumentList.e.isNodeSet() == false)
            throw new IllegalArgumentException(
                    "Count()'s argument count is invalid");
        vn.push2();
        try {
            a = 0;
            argumentList.e.adjust(vn.getTokenCount());
            while (argumentList.e.evalNodeSet(vn) != -1) {
                a++;
            }
            argumentList.e.reset(vn);
            vn.pop2();
        } catch (Exception e) {
            argumentList.e.reset(vn);
            vn.pop2();
        }
        return a;
    }

    private double sum(VTDNav vn) {
        int d = 0;
        if (argCount() != 1 || argumentList.e.isNodeSet() == false)
            throw new IllegalArgumentException(
                    "sum()'s argument count is invalid");
        vn.push2();
        try {
            a = 0;
            int i1;
            while ((a = argumentList.e.evalNodeSet(vn)) != -1) {
                int t = vn.getTokenType(a);
                if (t == VTDNav.TOKEN_STARTING_TAG) {
                    i1 = vn.getText();
                    if (i1 != -1)
                        d += vn.parseDouble(i1);
                    if (Double.isNaN(d))
                        break;
                } else if (t == VTDNav.TOKEN_ATTR_NAME
                        || t == VTDNav.TOKEN_ATTR_NS) {
                    d += vn.parseDouble(a + 1);
                    if (Double.isNaN(d))
                        break;
                } else if (t == VTDNav.TOKEN_CHARACTER_DATA
                        || t == VTDNav.TOKEN_CDATA_VAL) {
                    d += vn.parseDouble(a);
                    if (Double.isNaN(d))
                        break;
                }
                // fib1.append(i);
            }
            argumentList.e.reset(vn);
            vn.pop2();
            return d;
        } catch (Exception e) {
            argumentList.e.reset(vn);
            vn.pop2();
            return Double.NaN;
        }

    }

    // to support computer context size
    // needs to add

    public boolean requireContextSize() {
        if (opCode == FuncName.LAST)
            return true;
        else {
            Alist temp = argumentList;
            // boolean b = false;
            while (temp != null) {
                if (temp.e.requireContextSize()) {
                    return true;
                }
                temp = temp.next;
            }
        }
        return false;
    }

    public void setContextSize(int size) {
        if (opCode == FuncName.LAST) {
            contextSize = size;
            // System.out.println("contextSize: "+size);
        } else {
            Alist temp = argumentList;
            // boolean b = false;
            while (temp != null) {
                temp.e.setContextSize(size);
                temp = temp.next;
            }
        }
    }

    public void setPosition(int pos) {
        if (opCode == FuncName.POSITION) {
            position = pos;
            // System.out.println("PO: "+size);
        } else {
            Alist temp = argumentList;
            // boolean b = false;
            while (temp != null) {
                temp.e.setPosition(pos);
                temp = temp.next;
            }
        }
    }

    public int adjust(int n) {
        int i = 0;
        switch (opCode) {
        case FuncName.COUNT:
        case FuncName.SUM:
            i = argumentList.e.adjust(n);
            break;
        default:
        }
        return i;
    }

    // added by yanj at 2007-07-08
    private boolean gpWithin(VTDNav vn) {
        boolean result = false;
        long gmlreadtime = System.currentTimeMillis();
        Geometry g1 = null, g2 = null;
        Expr pe = argumentList.e;
        g1 = GMLReader.read(vn, pe);
        
        if (argumentList.next == null){
            g2 = g1;
            g1 = GMLReader.read(vn);
        } else {
            pe = argumentList.next.e;
            g2 = GMLReader.read(vn, pe);
        }
        gmlreadtime = System.currentTimeMillis() - gmlreadtime;
        
        if ((g1 == null) || (g2 == null))
            return result;
        
        long jtstime = System.currentTimeMillis();
        result = g1.within(g2);
        jtstime = System.currentTimeMillis() - jtstime;
        
        if (logger.isDebugEnabled()){
            String log = "Time consumed inside: transform - " + gmlreadtime;
            log += ";\t do(within)" + jtstime;
            logger.debug(log);
        }
        return result;
    }
    
    private String gpGeometryType(VTDNav vn){
        String result = "";
        long gmlreadtime = System.currentTimeMillis();
        if (argumentList != null){
            Expr pe = argumentList.e;
            if (pe.isString() == true){
                String pstr = pe.evalString(vn);
                result =  GMLReader.parseType(pstr);
            }else
                result = GMLReader.parseType(vn, pe);
        } else
            result = GMLReader.parseType(vn);
        gmlreadtime = System.currentTimeMillis() - gmlreadtime;
        
        if (logger.isDebugEnabled()){
            String log = "Time consumed inside: do(geometrytype) - " + gmlreadtime;
            logger.debug(log);
        }
        return result;
    }
    
    private String gpIntersection(VTDNav vn){
        String result = "";
        Geometry g1 = null, g2 = null;
        long gmlreadtime = System.currentTimeMillis();
        Expr pe = argumentList.e;
        g1 = GMLReader.read(vn, pe);
        
        if (argumentList.next == null){
            g2 = g1;
            g1 = GMLReader.read(vn);
        } else {
            pe = argumentList.next.e;
            g2 = GMLReader.read(vn, pe);
        }
        gmlreadtime = System.currentTimeMillis() - gmlreadtime;
        
        if ((g1 == null) || (g2 == null))
            return result;
        
        long jtstime = System.currentTimeMillis();
        Geometry g = g1.intersection(g2);
        if (g.isEmpty() == false)
            result = g.toText();  //return WKT temporarily
        jtstime = System.currentTimeMillis() - jtstime;
        
        if (logger.isDebugEnabled()){
            String log = "Time consumed inside: transform - " + gmlreadtime;
            log += ";\t do(intersection)" + jtstime;
            logger.debug(log);
        }
        return result;
    }
    // added end
}
