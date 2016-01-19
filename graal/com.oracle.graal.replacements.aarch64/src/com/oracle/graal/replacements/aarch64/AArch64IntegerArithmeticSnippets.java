/*
 * Copyright (c) 2014, 2015, Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.graal.replacements.aarch64;

import com.oracle.graal.api.replacements.SnippetReflectionProvider;
import com.oracle.graal.graph.Node.ConstantNodeParameter;
import com.oracle.graal.graph.Node.NodeIntrinsic;
import com.oracle.graal.graph.NodeClass;
import com.oracle.graal.nodeinfo.NodeInfo;
import com.oracle.graal.nodes.ValueNode;
import com.oracle.graal.nodes.calc.FixedBinaryNode;
import com.oracle.graal.nodes.calc.IntegerDivNode;
import com.oracle.graal.nodes.calc.IntegerRemNode;
import com.oracle.graal.nodes.calc.UnsignedDivNode;
import com.oracle.graal.nodes.calc.UnsignedRemNode;
import com.oracle.graal.nodes.spi.LoweringTool;
import com.oracle.graal.phases.util.Providers;
import com.oracle.graal.replacements.Snippet;
import com.oracle.graal.replacements.SnippetTemplate;
import com.oracle.graal.replacements.SnippetTemplate.AbstractTemplates;
import com.oracle.graal.replacements.Snippets;

import jdk.vm.ci.code.TargetDescription;
import jdk.vm.ci.common.JVMCIError;
import jdk.vm.ci.meta.JavaKind;

/**
 * Division in AArch64 ISA does not generate a trap when dividing by zero, but instead sets the
 * result to 0. These snippets throw an ArithmethicException if the denominator is 0 and otherwise
 * forward to the LIRGenerator.
 */
public class AArch64IntegerArithmeticSnippets extends AbstractTemplates implements Snippets {

    private final SnippetTemplate.SnippetInfo idiv;
    private final SnippetTemplate.SnippetInfo ldiv;
    private final SnippetTemplate.SnippetInfo irem;
    private final SnippetTemplate.SnippetInfo lrem;

    private final SnippetTemplate.SnippetInfo uidiv;
    private final SnippetTemplate.SnippetInfo uldiv;
    private final SnippetTemplate.SnippetInfo uirem;
    private final SnippetTemplate.SnippetInfo ulrem;

    public AArch64IntegerArithmeticSnippets(Providers providers, SnippetReflectionProvider snippetReflection, TargetDescription target) {
        super(providers, snippetReflection, target);
        idiv = snippet(AArch64IntegerArithmeticSnippets.class, "idivSnippet");
        ldiv = snippet(AArch64IntegerArithmeticSnippets.class, "ldivSnippet");
        irem = snippet(AArch64IntegerArithmeticSnippets.class, "iremSnippet");
        lrem = snippet(AArch64IntegerArithmeticSnippets.class, "lremSnippet");

        uidiv = snippet(AArch64IntegerArithmeticSnippets.class, "uidivSnippet");
        uldiv = snippet(AArch64IntegerArithmeticSnippets.class, "uldivSnippet");
        uirem = snippet(AArch64IntegerArithmeticSnippets.class, "uiremSnippet");
        ulrem = snippet(AArch64IntegerArithmeticSnippets.class, "ulremSnippet");
    }

    public void lower(FixedBinaryNode node, LoweringTool tool) {
        // assert node.kind() == JavaKind.Int || node.kind() == JavaKind.Long;
        // SnippetTemplate.SnippetInfo snippet;
        // if (node instanceof SafeNode) {
        // // We already introduced the 0 division check, nothing to do.
        // return;
        // } else if (node instanceof IntegerDivNode) {
        // snippet = node.kind() == JavaKind.Int ? idiv : ldiv;
        // } else if (node instanceof IntegerRemNode) {
        // snippet = node.kind() == JavaKind.Int ? irem : lrem;
        // } else if (node instanceof UnsignedDivNode) {
        // snippet = node.kind() == JavaKind.Int ? uidiv : uldiv;
        // } else if (node instanceof UnsignedRemNode) {
        // snippet = node.kind() == JavaKind.Int ? uirem : ulrem;
        // } else {
        // throw GraalInternalError.shouldNotReachHere();
        // }
        // Arguments args = new Arguments(snippet, node.graph().getGuardsStage());
        // args.add("x", node.x());
        // args.add("y", node.y());
        // template(args).instantiate(providers.getMetaAccess(), node,
        // SnippetTemplate.DEFAULT_REPLACER,
        // args);
        throw JVMCIError.unimplemented(node + ", " + tool);
    }

    @Snippet
    public static int idivSnippet(int x, int y) {
        checkForZero(y);
        return safeDiv(JavaKind.Int, x, y);
    }

    @Snippet
    public static long ldivSnippet(long x, long y) {
        checkForZero(y);
        return safeDiv(JavaKind.Long, x, y);
    }

    @Snippet
    public static int iremSnippet(int x, int y) {
        checkForZero(y);
        return safeRem(JavaKind.Int, x, y);
    }

    @Snippet
    public static long lremSnippet(long x, long y) {
        checkForZero(y);
        return safeRem(JavaKind.Long, x, y);
    }

    @Snippet
    public static int uidivSnippet(int x, int y) {
        checkForZero(y);
        return safeUDiv(JavaKind.Int, x, y);
    }

    @Snippet
    public static long uldivSnippet(long x, long y) {
        checkForZero(y);
        return safeUDiv(JavaKind.Long, x, y);
    }

    @Snippet
    public static int uiremSnippet(int x, int y) {
        checkForZero(y);
        return safeURem(JavaKind.Int, x, y);
    }

    @Snippet
    public static long ulremSnippet(long x, long y) {
        checkForZero(y);
        return safeURem(JavaKind.Long, x, y);
    }

    private static void checkForZero(int y) {
        if (y == 0) {
            throw new ArithmeticException("/ by zero.");
        }
    }

    private static void checkForZero(long y) {
        if (y == 0) {
            throw new ArithmeticException("/ by zero.");
        }
    }

    @NodeIntrinsic(SafeIntegerDivNode.class)
    private static native int safeDiv(@ConstantNodeParameter JavaKind kind, int x, int y);

    @NodeIntrinsic(SafeIntegerDivNode.class)
    private static native long safeDiv(@ConstantNodeParameter JavaKind kind, long x, long y);

    @NodeIntrinsic(SafeIntegerRemNode.class)
    private static native int safeRem(@ConstantNodeParameter JavaKind kind, int x, int y);

    @NodeIntrinsic(SafeIntegerRemNode.class)
    private static native long safeRem(@ConstantNodeParameter JavaKind kind, long x, long y);

    @NodeIntrinsic(SafeUnsignedDivNode.class)
    private static native int safeUDiv(@ConstantNodeParameter JavaKind kind, int x, int y);

    @NodeIntrinsic(SafeUnsignedDivNode.class)
    private static native long safeUDiv(@ConstantNodeParameter JavaKind kind, long x, long y);

    @NodeIntrinsic(SafeUnsignedRemNode.class)
    private static native int safeURem(@ConstantNodeParameter JavaKind kind, int x, int y);

    @NodeIntrinsic(SafeUnsignedRemNode.class)
    private static native long safeURem(@ConstantNodeParameter JavaKind kind, long x, long y);

    // Marker interface to distinguish untreated nodes from ones where we have installed the
    // additional checks
    private interface SafeNode {
    }

    @NodeInfo
    static class SafeIntegerDivNode extends IntegerDivNode implements SafeNode {
        public static final NodeClass<SafeIntegerDivNode> TYPE = NodeClass.create(SafeIntegerDivNode.class);

        @SuppressWarnings("unused")
        protected SafeIntegerDivNode(JavaKind kind, ValueNode x, ValueNode y) {
            super(x, y);
        }
    }

    @NodeInfo
    static class SafeIntegerRemNode extends IntegerRemNode implements SafeNode {
        public static final NodeClass<SafeIntegerRemNode> TYPE = NodeClass.create(SafeIntegerRemNode.class);

        @SuppressWarnings("unused")
        protected SafeIntegerRemNode(JavaKind kind, ValueNode x, ValueNode y) {
            super(x, y);
        }
    }

    @NodeInfo
    static class SafeUnsignedDivNode extends UnsignedDivNode implements SafeNode {
        public static final NodeClass<SafeUnsignedDivNode> TYPE = NodeClass.create(SafeUnsignedDivNode.class);

        @SuppressWarnings("unused")
        protected SafeUnsignedDivNode(JavaKind kind, ValueNode x, ValueNode y) {
            super(x, y);
        }
    }

    @NodeInfo
    static class SafeUnsignedRemNode extends UnsignedRemNode implements SafeNode {
        public static final NodeClass<SafeUnsignedRemNode> TYPE = NodeClass.create(SafeUnsignedRemNode.class);

        @SuppressWarnings("unused")
        protected SafeUnsignedRemNode(JavaKind kind, ValueNode x, ValueNode y) {
            super(x, y);
        }
    }
}
