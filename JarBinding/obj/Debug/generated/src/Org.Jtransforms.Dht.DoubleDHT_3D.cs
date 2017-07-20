using System;
using System.Collections.Generic;
using Android.Runtime;

namespace Org.Jtransforms.Dht {

	// Metadata.xml XPath class reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']"
	[global::Android.Runtime.Register ("org/jtransforms/dht/DoubleDHT_3D", DoNotGenerateAcw=true)]
	public partial class DoubleDHT_3D : global::Java.Lang.Object {

		internal static IntPtr java_class_handle;
		internal static IntPtr class_ref {
			get {
				return JNIEnv.FindClass ("org/jtransforms/dht/DoubleDHT_3D", ref java_class_handle);
			}
		}

		protected override IntPtr ThresholdClass {
			get { return class_ref; }
		}

		protected override global::System.Type ThresholdType {
			get { return typeof (DoubleDHT_3D); }
		}

		protected DoubleDHT_3D (IntPtr javaReference, JniHandleOwnership transfer) : base (javaReference, transfer) {}

		static IntPtr id_ctor_JJJ;
		// Metadata.xml XPath constructor reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/constructor[@name='DoubleDHT_3D' and count(parameter)=3 and parameter[1][@type='long'] and parameter[2][@type='long'] and parameter[3][@type='long']]"
		[Register (".ctor", "(JJJ)V", "")]
		public unsafe DoubleDHT_3D (long p0, long p1, long p2)
			: base (IntPtr.Zero, JniHandleOwnership.DoNotTransfer)
		{
			if (((global::Java.Lang.Object) this).Handle != IntPtr.Zero)
				return;

			try {
				JValue* __args = stackalloc JValue [3];
				__args [0] = new JValue (p0);
				__args [1] = new JValue (p1);
				__args [2] = new JValue (p2);
				if (((object) this).GetType () != typeof (DoubleDHT_3D)) {
					SetHandle (
							global::Android.Runtime.JNIEnv.StartCreateInstance (((object) this).GetType (), "(JJJ)V", __args),
							JniHandleOwnership.TransferLocalRef);
					global::Android.Runtime.JNIEnv.FinishCreateInstance (((global::Java.Lang.Object) this).Handle, "(JJJ)V", __args);
					return;
				}

				if (id_ctor_JJJ == IntPtr.Zero)
					id_ctor_JJJ = JNIEnv.GetMethodID (class_ref, "<init>", "(JJJ)V");
				SetHandle (
						global::Android.Runtime.JNIEnv.StartCreateInstance (class_ref, id_ctor_JJJ, __args),
						JniHandleOwnership.TransferLocalRef);
				JNIEnv.FinishCreateInstance (((global::Java.Lang.Object) this).Handle, class_ref, id_ctor_JJJ, __args);
			} finally {
			}
		}

		static Delegate cb_forward_arrayD;
#pragma warning disable 0169
		static Delegate GetForward_arrayDHandler ()
		{
			if (cb_forward_arrayD == null)
				cb_forward_arrayD = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr>) n_Forward_arrayD);
			return cb_forward_arrayD;
		}

		static void n_Forward_arrayD (IntPtr jnienv, IntPtr native__this, IntPtr native_p0)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			double[] p0 = (double[]) JNIEnv.GetArray (native_p0, JniHandleOwnership.DoNotTransfer, typeof (double));
			__this.Forward (p0);
			if (p0 != null)
				JNIEnv.CopyArray (p0, native_p0);
		}
#pragma warning restore 0169

		static IntPtr id_forward_arrayD;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='forward' and count(parameter)=1 and parameter[1][@type='double[]']]"
		[Register ("forward", "([D)V", "GetForward_arrayDHandler")]
		public virtual unsafe void Forward (double[] p0)
		{
			if (id_forward_arrayD == IntPtr.Zero)
				id_forward_arrayD = JNIEnv.GetMethodID (class_ref, "forward", "([D)V");
			IntPtr native_p0 = JNIEnv.NewArray (p0);
			try {
				JValue* __args = stackalloc JValue [1];
				__args [0] = new JValue (native_p0);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_forward_arrayD, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "forward", "([D)V"), __args);
			} finally {
				if (p0 != null) {
					JNIEnv.CopyArray (native_p0, p0);
					JNIEnv.DeleteLocalRef (native_p0);
				}
			}
		}

		static Delegate cb_forward_arrayarrayarrayD;
#pragma warning disable 0169
		static Delegate GetForward_arrayarrayarrayDHandler ()
		{
			if (cb_forward_arrayarrayarrayD == null)
				cb_forward_arrayarrayarrayD = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr>) n_Forward_arrayarrayarrayD);
			return cb_forward_arrayarrayarrayD;
		}

		static void n_Forward_arrayarrayarrayD (IntPtr jnienv, IntPtr native__this, IntPtr native_p0)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			double[][][] p0 = (double[][][]) JNIEnv.GetArray (native_p0, JniHandleOwnership.DoNotTransfer, typeof (double[][]));
			__this.Forward (p0);
			if (p0 != null)
				JNIEnv.CopyArray (p0, native_p0);
		}
#pragma warning restore 0169

		static IntPtr id_forward_arrayarrayarrayD;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='forward' and count(parameter)=1 and parameter[1][@type='double[][][]']]"
		[Register ("forward", "([[[D)V", "GetForward_arrayarrayarrayDHandler")]
		public virtual unsafe void Forward (double[][][] p0)
		{
			if (id_forward_arrayarrayarrayD == IntPtr.Zero)
				id_forward_arrayarrayarrayD = JNIEnv.GetMethodID (class_ref, "forward", "([[[D)V");
			IntPtr native_p0 = JNIEnv.NewArray (p0);
			try {
				JValue* __args = stackalloc JValue [1];
				__args [0] = new JValue (native_p0);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_forward_arrayarrayarrayD, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "forward", "([[[D)V"), __args);
			} finally {
				if (p0 != null) {
					JNIEnv.CopyArray (native_p0, p0);
					JNIEnv.DeleteLocalRef (native_p0);
				}
			}
		}

		static Delegate cb_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_;
#pragma warning disable 0169
		static Delegate GetForward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Handler ()
		{
			if (cb_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ == null)
				cb_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr>) n_Forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_);
			return cb_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_;
		}

		static void n_Forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ (IntPtr jnienv, IntPtr native__this, IntPtr native_p0)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray p0 = global::Java.Lang.Object.GetObject<global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray> (native_p0, JniHandleOwnership.DoNotTransfer);
			__this.Forward (p0);
		}
#pragma warning restore 0169

		static IntPtr id_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='forward' and count(parameter)=1 and parameter[1][@type='pl.edu.icm.jlargearrays.DoubleLargeArray']]"
		[Register ("forward", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;)V", "GetForward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Handler")]
		public virtual unsafe void Forward (global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray p0)
		{
			if (id_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ == IntPtr.Zero)
				id_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ = JNIEnv.GetMethodID (class_ref, "forward", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;)V");
			try {
				JValue* __args = stackalloc JValue [1];
				__args [0] = new JValue (p0);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_forward_Lpl_edu_icm_jlargearrays_DoubleLargeArray_, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "forward", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;)V"), __args);
			} finally {
			}
		}

		static Delegate cb_inverse_arrayDZ;
#pragma warning disable 0169
		static Delegate GetInverse_arrayDZHandler ()
		{
			if (cb_inverse_arrayDZ == null)
				cb_inverse_arrayDZ = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr, bool>) n_Inverse_arrayDZ);
			return cb_inverse_arrayDZ;
		}

		static void n_Inverse_arrayDZ (IntPtr jnienv, IntPtr native__this, IntPtr native_p0, bool p1)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			double[] p0 = (double[]) JNIEnv.GetArray (native_p0, JniHandleOwnership.DoNotTransfer, typeof (double));
			__this.Inverse (p0, p1);
			if (p0 != null)
				JNIEnv.CopyArray (p0, native_p0);
		}
#pragma warning restore 0169

		static IntPtr id_inverse_arrayDZ;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='inverse' and count(parameter)=2 and parameter[1][@type='double[]'] and parameter[2][@type='boolean']]"
		[Register ("inverse", "([DZ)V", "GetInverse_arrayDZHandler")]
		public virtual unsafe void Inverse (double[] p0, bool p1)
		{
			if (id_inverse_arrayDZ == IntPtr.Zero)
				id_inverse_arrayDZ = JNIEnv.GetMethodID (class_ref, "inverse", "([DZ)V");
			IntPtr native_p0 = JNIEnv.NewArray (p0);
			try {
				JValue* __args = stackalloc JValue [2];
				__args [0] = new JValue (native_p0);
				__args [1] = new JValue (p1);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_inverse_arrayDZ, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "inverse", "([DZ)V"), __args);
			} finally {
				if (p0 != null) {
					JNIEnv.CopyArray (native_p0, p0);
					JNIEnv.DeleteLocalRef (native_p0);
				}
			}
		}

		static Delegate cb_inverse_arrayarrayarrayDZ;
#pragma warning disable 0169
		static Delegate GetInverse_arrayarrayarrayDZHandler ()
		{
			if (cb_inverse_arrayarrayarrayDZ == null)
				cb_inverse_arrayarrayarrayDZ = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr, bool>) n_Inverse_arrayarrayarrayDZ);
			return cb_inverse_arrayarrayarrayDZ;
		}

		static void n_Inverse_arrayarrayarrayDZ (IntPtr jnienv, IntPtr native__this, IntPtr native_p0, bool p1)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			double[][][] p0 = (double[][][]) JNIEnv.GetArray (native_p0, JniHandleOwnership.DoNotTransfer, typeof (double[][]));
			__this.Inverse (p0, p1);
			if (p0 != null)
				JNIEnv.CopyArray (p0, native_p0);
		}
#pragma warning restore 0169

		static IntPtr id_inverse_arrayarrayarrayDZ;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='inverse' and count(parameter)=2 and parameter[1][@type='double[][][]'] and parameter[2][@type='boolean']]"
		[Register ("inverse", "([[[DZ)V", "GetInverse_arrayarrayarrayDZHandler")]
		public virtual unsafe void Inverse (double[][][] p0, bool p1)
		{
			if (id_inverse_arrayarrayarrayDZ == IntPtr.Zero)
				id_inverse_arrayarrayarrayDZ = JNIEnv.GetMethodID (class_ref, "inverse", "([[[DZ)V");
			IntPtr native_p0 = JNIEnv.NewArray (p0);
			try {
				JValue* __args = stackalloc JValue [2];
				__args [0] = new JValue (native_p0);
				__args [1] = new JValue (p1);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_inverse_arrayarrayarrayDZ, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "inverse", "([[[DZ)V"), __args);
			} finally {
				if (p0 != null) {
					JNIEnv.CopyArray (native_p0, p0);
					JNIEnv.DeleteLocalRef (native_p0);
				}
			}
		}

		static Delegate cb_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z;
#pragma warning disable 0169
		static Delegate GetInverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ZHandler ()
		{
			if (cb_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z == null)
				cb_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z = JNINativeWrapper.CreateDelegate ((Action<IntPtr, IntPtr, IntPtr, bool>) n_Inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z);
			return cb_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z;
		}

		static void n_Inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z (IntPtr jnienv, IntPtr native__this, IntPtr native_p0, bool p1)
		{
			global::Org.Jtransforms.Dht.DoubleDHT_3D __this = global::Java.Lang.Object.GetObject<global::Org.Jtransforms.Dht.DoubleDHT_3D> (jnienv, native__this, JniHandleOwnership.DoNotTransfer);
			global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray p0 = global::Java.Lang.Object.GetObject<global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray> (native_p0, JniHandleOwnership.DoNotTransfer);
			__this.Inverse (p0, p1);
		}
#pragma warning restore 0169

		static IntPtr id_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z;
		// Metadata.xml XPath method reference: path="/api/package[@name='org.jtransforms.dht']/class[@name='DoubleDHT_3D']/method[@name='inverse' and count(parameter)=2 and parameter[1][@type='pl.edu.icm.jlargearrays.DoubleLargeArray'] and parameter[2][@type='boolean']]"
		[Register ("inverse", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;Z)V", "GetInverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_ZHandler")]
		public virtual unsafe void Inverse (global::PL.Edu.Icm.Jlargearrays.DoubleLargeArray p0, bool p1)
		{
			if (id_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z == IntPtr.Zero)
				id_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z = JNIEnv.GetMethodID (class_ref, "inverse", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;Z)V");
			try {
				JValue* __args = stackalloc JValue [2];
				__args [0] = new JValue (p0);
				__args [1] = new JValue (p1);

				if (((object) this).GetType () == ThresholdType)
					JNIEnv.CallVoidMethod (((global::Java.Lang.Object) this).Handle, id_inverse_Lpl_edu_icm_jlargearrays_DoubleLargeArray_Z, __args);
				else
					JNIEnv.CallNonvirtualVoidMethod (((global::Java.Lang.Object) this).Handle, ThresholdClass, JNIEnv.GetMethodID (ThresholdClass, "inverse", "(Lpl/edu/icm/jlargearrays/DoubleLargeArray;Z)V"), __args);
			} finally {
			}
		}

	}
}