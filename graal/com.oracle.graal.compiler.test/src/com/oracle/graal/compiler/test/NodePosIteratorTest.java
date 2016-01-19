/*
 * Copyright (c) 2012, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.graal.compiler.test;

import org.junit.Assert;
import org.junit.Test;

import com.oracle.graal.graph.Node;
import com.oracle.graal.graph.NodeClass;
import com.oracle.graal.graph.NodeClassIterable;
import com.oracle.graal.graph.NodeInputList;
import com.oracle.graal.graph.NodePosIterator;
import com.oracle.graal.graph.NodeSuccessorList;
import com.oracle.graal.nodeinfo.NodeInfo;
import com.oracle.graal.nodes.ConstantNode;
import com.oracle.graal.nodes.EndNode;
import com.oracle.graal.nodes.ValueNode;
import com.oracle.graal.nodes.calc.FloatingNode;

public class NodePosIteratorTest extends GraalCompilerTest {

    @NodeInfo
    static final class TestNode extends Node {
        public static final NodeClass<TestNode> TYPE = NodeClass.create(TestNode.class);
        @Successor Node s1;
        @Successor Node s2;
        @Successor NodeSuccessorList<Node> stail;

        @Input NodeInputList<ValueNode> itail;
        @Input ConstantNode i1;
        @Input FloatingNode i2;

        protected TestNode() {
            super(TYPE);
        }

    }

    @Test
    public void testInputs() {
        TestNode n = new TestNode();

        ConstantNode i1 = ConstantNode.forInt(1);
        ConstantNode i2 = ConstantNode.forDouble(1.0d);
        ConstantNode i3 = ConstantNode.forInt(4);
        ConstantNode i4 = ConstantNode.forInt(14);
        n.itail = new NodeInputList<>(n, new ValueNode[]{i3, i4});
        n.i1 = i1;
        n.i2 = i2;

        NodeClassIterable inputs = n.inputs();

        NodePosIterator iterator = inputs.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i1);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i2);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i3);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i4);
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.hasNext());

        iterator = inputs.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("ConstantNode:i1", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("FloatingNode:i2", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("NodeInputList:itail[0]", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("NodeInputList:itail[1]", iterator.nextPosition().toString());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.hasNext());

        iterator = inputs.iterator();
        n.i1 = i4;
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i4);
        n.i2 = i1;
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i1);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i3);
        n.itail.initialize(1, i4);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i4);
        Assert.assertFalse(iterator.hasNext());

        iterator = inputs.iterator();
        n.i1 = null;
        n.i2 = i2;
        n.itail.initialize(0, null);
        n.itail.initialize(1, i4);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i2);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i4);
        Assert.assertFalse(iterator.hasNext());

        iterator = inputs.withNullIterator();
        n.i1 = null;
        n.i2 = null;
        n.itail.initialize(0, i3);
        n.itail.initialize(1, null);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), i3);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testSuccessors() {
        TestNode n = new TestNode();
        EndNode s1 = new EndNode();
        EndNode s2 = new EndNode();
        EndNode s3 = new EndNode();
        EndNode s4 = new EndNode();
        n.s1 = s1;
        n.s2 = s2;
        n.stail = new NodeSuccessorList<>(n, new Node[]{s3, s4});

        NodeClassIterable successors = n.successors();
        NodePosIterator iterator = successors.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s1);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s2);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s3);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s4);
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.hasNext());

        iterator = successors.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(Node.class.getSimpleName() + ":s1", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(Node.class.getSimpleName() + ":s2", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(NodeSuccessorList.class.getSimpleName() + ":stail[0]", iterator.nextPosition().toString());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(NodeSuccessorList.class.getSimpleName() + ":stail[1]", iterator.nextPosition().toString());
        Assert.assertFalse(iterator.hasNext());
        Assert.assertFalse(iterator.hasNext());

        iterator = successors.iterator();
        n.s1 = s4;
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s4);
        n.s2 = s1;
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s1);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s3);
        n.stail.initialize(1, s4);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s4);
        Assert.assertFalse(iterator.hasNext());

        iterator = successors.iterator();
        n.s1 = null;
        n.s2 = s2;
        n.stail.initialize(0, null);
        n.stail.initialize(1, s4);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s2);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s4);
        Assert.assertFalse(iterator.hasNext());

        iterator = successors.withNullIterator();
        n.s1 = null;
        n.s2 = null;
        n.stail.initialize(0, s3);
        n.stail.initialize(1, null);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals(iterator.next(), s3);
        Assert.assertTrue(iterator.hasNext());
        Assert.assertNull(iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }
}
