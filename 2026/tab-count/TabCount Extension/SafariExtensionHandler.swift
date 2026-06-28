//
//  SafariExtensionHandler.swift
//  TabCount Extension
//
//  Created by Gary Verhaegen on 2019-09-28.
//  Copyright © 2019 Sotenj. All rights reserved.
//

import SafariServices

class SafariExtensionHandler: SFSafariExtensionHandler {

    override func validateToolbarItem(in _: SFSafariWindow, validationHandler: @escaping ((Bool, String) -> Void)) {
        SFSafariApplication.getAllWindows { windows in
            let group = DispatchGroup()
            let lock = NSLock()
            var count = 0

            for window in windows {
                group.enter()
                window.getAllTabs { tabs in
                    lock.lock()
                    count += tabs.count
                    lock.unlock()
                    group.leave()
                }
            }

            group.notify(queue: .main) {
                validationHandler(true, "\(count)")
            }
        }
    }

    override func popoverViewController() -> SFSafariExtensionViewController {
        return SafariExtensionViewController.shared
    }

}
