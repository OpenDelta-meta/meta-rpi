DESCRIPTION = "This repository contains the source code for the ARM side \
libraries used on Raspberry Pi. These typically are installed in /opt/vc/lib \
and includes source for the ARM side code to interface to: EGL, mmal, GLESv2,\
vcos, openmaxil, vchiq_arm, bcm_host, WFC, OpenVG."
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENCE;md5=0448d6488ef8cc380632b1569ee6d196"

PROVIDES += "virtual/libgles2 virtual/egl virtual/libomxil"

RPROVIDES_${PN} += "libgles2 egl libegl libegl1 libglesv2-2"

COMPATIBLE_MACHINE = "^(raspberrypi|raspberrypi0|raspberrypi2|raspberrypi3|raspberrypi4)$"

SRCBRANCH = "master"
SRCFORK = "raspberrypi"
SRCREV = "9f3f9054a692e53b60fca54221a402414e030335"

# Use the date of the above commit as the package version. Update this when
# SRCREV is changed.
PV = "20201027"

SRC_URI = "\
	git://github.com/${SRCFORK}/userland.git;protocol=git;branch=${SRCBRANCH} \
	file://0001-Allow-applications-to-set-next-resource-handle.patch \
	file://0002-wayland-Add-support-for-the-Wayland-winsys.patch \
	file://0003-wayland-Add-Wayland-example.patch \
	file://0004-wayland-egl-Add-bcm_host-to-dependencies.patch \
	file://0005-interface-remove-faulty-assert-to-make-weston-happy-.patch \
	file://0006-zero-out-wl-buffers-in-egl_surface_free.patch \
	file://0007-initialize-front-back-wayland-buffers.patch \
	file://0008-Remove-RPC_FLUSH.patch \
	file://0009-fix-cmake-dependency-race.patch \
	file://0010-Fix-for-framerate-with-nested-composition.patch \
	file://0011-build-shared-library-for-vchostif.patch \
	file://0012-implement-buffer-wrapping-interface-for-dispmanx.patch \
	file://0013-Implement-triple-buffering-for-wayland.patch \
	file://0014-GLES2-gl2ext.h-Define-GL_R8_EXT-and-GL_RG8_EXT.patch \
	file://0015-EGL-glplatform.h-define-EGL_CAST.patch \
	file://0016-Allow-multiple-wayland-compositor-state-data-per-pro.patch \
	file://0017-khronos-backport-typedef-for-EGL_EXT_image_dma_buf_i.patch \
	file://0018-Add-EGL_IMG_context_priority-related-defines.patch \
	file://0019-libfdt-Undefine-__wordsize-if-already-defined.patch \
	file://0020-openmaxil-add-pkg-config-file.patch \
	file://0021-cmake-Disable-format-overflow-warning-as-error.patch \
	"

S = "${WORKDIR}/git"

inherit cmake pkgconfig

ASNEEDED = ""

EXTRA_OECMAKE = "-DCMAKE_BUILD_TYPE=Release -DCMAKE_EXE_LINKER_FLAGS='-Wl,--no-as-needed' \
                 -DVMCS_INSTALL_PREFIX=${exec_prefix} \
"

EXTRA_OECMAKE_append_aarch64 = " -DARM64=ON "


PACKAGECONFIG ?= "${@bb.utils.contains('DISTRO_FEATURES', 'wayland', 'wayland', '', d)}"

PACKAGECONFIG[wayland] = "-DBUILD_WAYLAND=TRUE -DWAYLAND_SCANNER_EXECUTABLE:FILEPATH=${STAGING_BINDIR_NATIVE}/wayland-scanner,,wayland-native wayland"

CFLAGS_append = " -fPIC"

do_install_append () {
	for f in `find ${D}${includedir}/interface/vcos/ -name "*.h"`; do
		sed -i 's/include "vcos_platform.h"/include "pthreads\/vcos_platform.h"/g' ${f}
		sed -i 's/include "vcos_futex_mutex.h"/include "pthreads\/vcos_futex_mutex.h"/g' ${f}
		sed -i 's/include "vcos_platform_types.h"/include "pthreads\/vcos_platform_types.h"/g' ${f}
	done
        rm -rf ${D}${prefix}${sysconfdir}
        ln -sf brcmglesv2.pc ${D}${libdir}/pkgconfig/glesv2.pc
        ln -sf brcmegl.pc ${D}${libdir}/pkgconfig/egl.pc
        ln -sf brcmvg.pc ${D}${libdir}/pkgconfig/vg.pc
}

# Shared libs from userland package  build aren't versioned, so we need
# to force the .so files into the runtime package (and keep them
# out of -dev package).
FILES_SOLIBSDEV = ""

FILES_${PN} += "\
    ${libdir}/*.so \
    ${libdir}/plugins"

FILES_${PN}-dev += "${includedir} \
                   ${prefix}/src"

FILES_${PN}-doc += "${datadir}/install"

FILES_${PN}-dbg += "${libdir}/plugins/.debug"

RDEPENDS_${PN} += "bash"
