//
//  SafariExtensionViewController.swift
//  TabCount Extension
//
//  Created by Gary Verhaegen on 2019-09-28.
//  Copyright © 2019 Sotenj. All rights reserved.
//

import SafariServices

class SafariExtensionViewController: SFSafariExtensionViewController {
    
    static let shared: SafariExtensionViewController = {
        let shared = SafariExtensionViewController()
        shared.preferredContentSize = NSSize(width:320, height:240)
        return shared
    }()

}
